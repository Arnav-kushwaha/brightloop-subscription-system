package com.brightloop.system.config;

import com.brightloop.system.auth.Role;
import com.brightloop.system.auth.User;
import com.brightloop.system.auth.UserRepository;
import com.brightloop.system.discount.Discount;
import com.brightloop.system.discount.DiscountRepository;
import com.brightloop.system.discount.DiscountType;
import com.brightloop.system.invoice.Invoice;
import com.brightloop.system.invoice.InvoiceItem;
import com.brightloop.system.invoice.InvoiceRepository;
import com.brightloop.system.invoice.InvoiceStatus;
import com.brightloop.system.payment.Payment;
import com.brightloop.system.payment.PaymentMethod;
import com.brightloop.system.payment.PaymentRepository;
import com.brightloop.system.plan.BillingInterval;
import com.brightloop.system.plan.SubscriptionPlan;
import com.brightloop.system.plan.SubscriptionPlanRepository;
import com.brightloop.system.product.Product;
import com.brightloop.system.product.ProductRepository;
import com.brightloop.system.product.ProductType;
import com.brightloop.system.product.ProductVariant;
import com.brightloop.system.subscription.Subscription;
import com.brightloop.system.subscription.SubscriptionRepository;
import com.brightloop.system.subscription.SubscriptionStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SubscriptionPlanRepository planRepository;
    private final DiscountRepository discountRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ProductRepository productRepository,
                      SubscriptionPlanRepository planRepository,
                      DiscountRepository discountRepository,
                      SubscriptionRepository subscriptionRepository,
                      InvoiceRepository invoiceRepository,
                      PaymentRepository paymentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.planRepository = planRepository;
        this.discountRepository = discountRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedProducts();
        seedPlans();
        seedDiscounts();
        seedDemoData();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@brightloop.com")) {
            User admin = new User(
                    "admin@brightloop.com",
                    passwordEncoder.encode("Admin@123"),
                    "Arnav Kushwaha",
                    Role.ADMIN
            );
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("staff@brightloop.com")) {
            User staff = new User(
                    "staff@brightloop.com",
                    passwordEncoder.encode("Staff@123"),
                    "Aditi Sharma",
                    Role.STAFF
            );
            userRepository.save(staff);
        }

        if (!userRepository.existsByEmail("customer@brightloop.com")) {
            User customer = new User(
                    "customer@brightloop.com",
                    passwordEncoder.encode("Customer@123"),
                    "Acme Global Inc.",
                    Role.CUSTOMER
            );
            userRepository.save(customer);
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            Product cloudSuite = new Product(
                    "Brightloop Cloud Suite",
                    ProductType.SOFTWARE,
                    new BigDecimal("49.00"),
                    new BigDecimal("15.00"),
                    "Flagship multi-tenant SaaS workspace"
            );
            cloudSuite.addVariant(new ProductVariant(cloudSuite, "Pro", new BigDecimal("20.00"), "BCS-PRO"));
            cloudSuite.addVariant(new ProductVariant(cloudSuite, "Enterprise", new BigDecimal("50.00"), "BCS-ENT"));
            productRepository.save(cloudSuite);

            Product dedicatedManager = new Product(
                    "Dedicated Account Concierge",
                    ProductType.SERVICE,
                    new BigDecimal("120.00"),
                    new BigDecimal("40.00"),
                    "24/7 prioritized SLA response and quarterly architecture reviews"
            );
            productRepository.save(dedicatedManager);

            Product iotGateway = new Product(
                    "Smart Sensor Gateway",
                    ProductType.PHYSICAL,
                    new BigDecimal("75.00"),
                    new BigDecimal("35.00"),
                    "Plug-and-play telemetric edge gateway"
            );
            productRepository.save(iotGateway);
        }
    }

    private void seedPlans() {
        if (planRepository.count() == 0) {
            SubscriptionPlan starterMonthly = new SubscriptionPlan(
                    "Starter Monthly",
                    BillingInterval.MONTHLY,
                    1,
                    new BigDecimal("29.99"),
                    1,
                    LocalDate.now().minusMonths(2),
                    null,
                    "Essential features for growing teams"
            );
            planRepository.save(starterMonthly);

            SubscriptionPlan businessMonthly = new SubscriptionPlan(
                    "Business Pro Monthly",
                    BillingInterval.MONTHLY,
                    1,
                    new BigDecimal("79.99"),
                    1,
                    LocalDate.now().minusMonths(3),
                    null,
                    "Full-featured subscription with advanced analytics and integrations"
            );
            planRepository.save(businessMonthly);

            SubscriptionPlan enterpriseAnnual = new SubscriptionPlan(
                    "Enterprise Annual",
                    BillingInterval.YEARLY,
                    1,
                    new BigDecimal("799.00"),
                    1,
                    LocalDate.now().minusMonths(6),
                    null,
                    "Comprehensive company-wide licensing with maximum discount"
            );
            planRepository.save(enterpriseAnnual);

            SubscriptionPlan sprintWeekly = new SubscriptionPlan(
                    "Sprint Weekly Pass",
                    BillingInterval.WEEKLY,
                    1,
                    new BigDecimal("12.50"),
                    1,
                    LocalDate.now().minusMonths(1),
                    null,
                    "Flexible 7-day sprint access"
            );
            planRepository.save(sprintWeekly);
        }
    }

    private void seedDiscounts() {
        if (discountRepository.count() == 0) {
            Discount flux2026 = new Discount(
                    "FLUX2026",
                    DiscountType.PERCENTAGE,
                    new BigDecimal("20.00"),
                    new BigDecimal("50.00"),
                    1,
                    LocalDate.now().minusDays(10),
                    LocalDate.now().plusMonths(3),
                    250
            );
            discountRepository.save(flux2026);

            Discount welcome50 = new Discount(
                    "WELCOME50",
                    DiscountType.FIXED,
                    new BigDecimal("50.00"),
                    new BigDecimal("100.00"),
                    1,
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusMonths(6),
                    100
            );
            discountRepository.save(welcome50);
        }
    }

    private void seedDemoData() {
        if (subscriptionRepository.count() == 0) {
            User customer = userRepository.findByEmail("customer@brightloop.com").orElse(null);
            SubscriptionPlan plan = planRepository.findAll().stream().findFirst().orElse(null);
            Product product = productRepository.findAll().stream().findFirst().orElse(null);

            if (customer != null && plan != null && product != null) {
                // Subscription 1: Active
                Subscription sub1 = new Subscription(
                        "SUB-2026-0001",
                        customer,
                        plan,
                        product,
                        null,
                        2,
                        plan.getBasePrice().add(product.getSellingPrice()),
                        plan.getBasePrice().add(product.getSellingPrice()).multiply(BigDecimal.valueOf(2)),
                        plan.getBillingInterval(),
                        LocalDate.now().minusMonths(1),
                        LocalDate.now().plusMonths(1),
                        LocalDate.now().plusDays(10),
                        SubscriptionStatus.ACTIVE,
                        null
                );
                subscriptionRepository.save(sub1);

                // Sample Invoice 1: Paid
                Invoice inv1 = new Invoice();
                inv1.setInvoiceNumber("INV-2026-0001");
                inv1.setCustomer(customer);
                inv1.setSubscription(sub1);
                inv1.setIssueDate(LocalDate.now().minusDays(20));
                inv1.setDueDate(LocalDate.now().minusDays(5));
                inv1.setStatus(InvoiceStatus.PAID);
                inv1.setBillingPeriodStart(LocalDate.now().minusMonths(1));
                inv1.setBillingPeriodEnd(LocalDate.now());
                inv1.setNotes("First billing cycle payment");

                BigDecimal subtotal1 = sub1.getRecurringAmount();
                inv1.setSubtotal(subtotal1);
                inv1.setDiscountAmount(BigDecimal.ZERO);
                BigDecimal tax1 = subtotal1.multiply(new BigDecimal("0.10"));
                inv1.setTaxRate(new BigDecimal("10.00"));
                inv1.setTaxAmount(tax1);
                BigDecimal total1 = subtotal1.add(tax1);
                inv1.setTotalAmount(total1);
                inv1.setPaidAmount(total1);
                inv1.setOutstandingAmount(BigDecimal.ZERO);

                InvoiceItem item1 = new InvoiceItem(inv1, plan.getPlanName() + " - " + product.getName(), 2, sub1.getUnitPrice(), subtotal1);
                inv1.addItem(item1);
                invoiceRepository.save(inv1);

                // Sample Payment 1
                Payment pay1 = new Payment(
                        "PAY-2026-0001",
                        inv1,
                        total1,
                        PaymentMethod.CREDIT_CARD,
                        LocalDateTime.now().minusDays(18),
                        "Automatic stripe card settlement",
                        "customer@brightloop.com"
                );
                paymentRepository.save(pay1);

                // Sample Invoice 2: Confirmed (Outstanding)
                Invoice inv2 = new Invoice();
                inv2.setInvoiceNumber("INV-2026-0002");
                inv2.setCustomer(customer);
                inv2.setSubscription(sub1);
                inv2.setIssueDate(LocalDate.now().minusDays(2));
                inv2.setDueDate(LocalDate.now().plusDays(13));
                inv2.setStatus(InvoiceStatus.CONFIRMED);
                inv2.setBillingPeriodStart(LocalDate.now());
                inv2.setBillingPeriodEnd(LocalDate.now().plusMonths(1));
                inv2.setNotes("Upcoming billing cycle statement");
                inv2.setSubtotal(subtotal1);
                inv2.setDiscountAmount(BigDecimal.ZERO);
                inv2.setTaxRate(new BigDecimal("10.00"));
                inv2.setTaxAmount(tax1);
                inv2.setTotalAmount(total1);
                inv2.setPaidAmount(BigDecimal.ZERO);
                inv2.setOutstandingAmount(total1);

                InvoiceItem item2 = new InvoiceItem(inv2, plan.getPlanName() + " - " + product.getName(), 2, sub1.getUnitPrice(), subtotal1);
                inv2.addItem(item2);
                invoiceRepository.save(inv2);
            }
        }
    }
}
