package tests;

import entities.Installment;
import entities.Order;
import entities.Payment;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FetchModifyUpdateTest {

    public static void main(String[] args) throws InterruptedException {
        new FetchModifyUpdateTest().run(args);
    }

    private void run(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        SessionFactory sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();

        seed(sessionFactory);

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            executorService.execute(() -> {
                calculateOrder(sessionFactory, finalI);
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        reportOrder(sessionFactory);

        System.exit(0);
    }

    private void calculateOrder(SessionFactory sessionFactory, int n) {
        Session session;
        session = sessionFactory.openSession();
        session.beginTransaction();

        Order order = getFirstOrder(session);

        session.refresh(order, LockMode.PESSIMISTIC_WRITE);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Payment newPayment = Payment.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal(1.0).setScale(2, RoundingMode.HALF_EVEN))
                .order(order)
                .build();
        order.getPayments().add(newPayment);

        BigDecimal totalAmountPaid = new BigDecimal(0.0).setScale(2, RoundingMode.HALF_EVEN);
        for (Payment payment : order.getPayments()) {
            totalAmountPaid = totalAmountPaid.add(payment.getAmount());
        }

        BigDecimal remainingPaid = new BigDecimal(totalAmountPaid.toString());
        for (Installment installment : order.getInstallments()) {
            int paidTotalComparison = remainingPaid.compareTo(installment.getTotalAmount());
            if (paidTotalComparison >= 0) {
                installment.setPaidAmount(installment.getTotalAmount());
                remainingPaid = remainingPaid.subtract(installment.getTotalAmount());
            } else {
                installment.setPaidAmount(remainingPaid);
                remainingPaid = remainingPaid.subtract(remainingPaid);
            }
        }
        order.setTotalAmountPaid(totalAmountPaid);

        session.getTransaction().commit();
        session.close();
    }

    private void reportOrder(SessionFactory sessionFactory) {
        Session session;
        session = sessionFactory.openSession();
        session.beginTransaction();
        Order order = getFirstOrder(session);
        System.err.println(order);
        for (Installment installment : order.getInstallments()) {
            System.err.println(String.format("  - %s", installment));
        }
        session.close();
    }

    private Order getFirstOrder(Session session) {
        Query query = session.createQuery("FROM Order o");
        query.setMaxResults(1);
        return (Order)query.list().stream().findFirst().orElse(null);
    }

    private void seed(SessionFactory sessionFactory) {
        Session session;
        session = sessionFactory.openSession();
        session.beginTransaction();

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            Order order = Order.builder()
                    .id(UUID.randomUUID())
                    .build();
            orders.add(order);

            for (int j = 0; j < 3; j++) {
                Installment installment = Installment.builder()
                        .id(UUID.randomUUID())
                        .order(order)
                        .totalAmount(new BigDecimal(2).setScale(2, RoundingMode.HALF_EVEN))
                        .build();
                order.getInstallments().add(installment);
            }
        }

        orders.forEach(session::save);

        session.getTransaction().commit();
        session.close();
    }

}
