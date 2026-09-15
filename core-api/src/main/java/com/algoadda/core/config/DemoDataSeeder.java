package com.algoadda.core.config;

import com.algoadda.core.bot.*;
import com.algoadda.core.bot.dto.BotUploadRequest;
import com.algoadda.core.bot.dto.PublishRequest;
import com.algoadda.core.bot.dto.PublishResponse;
import com.algoadda.core.bot.service.BotService;
import com.algoadda.core.compliance.ComplianceService;
import com.algoadda.core.listing.LicenseType;
import com.algoadda.core.listing.ListingRepository;
import com.algoadda.core.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("seed-demo")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final BotRepository botRepository;
    private final BotVersionRepository botVersionRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final ListingRepository listingRepository;
    private final BotService botService;
    private final ComplianceService complianceService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${algoadda.official-seller-email:official@algoadda.com}")
    private String officialSellerEmail;

    public DemoDataSeeder(
        UserRepository userRepository,
        SellerProfileRepository sellerProfileRepository,
        BotRepository botRepository,
        BotVersionRepository botVersionRepository,
        BacktestResultRepository backtestResultRepository,
        ListingRepository listingRepository,
        BotService botService,
        ComplianceService complianceService,
        PasswordEncoder passwordEncoder,
        ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.botRepository = botRepository;
        this.botVersionRepository = botVersionRepository;
        this.backtestResultRepository = backtestResultRepository;
        this.listingRepository = listingRepository;
        this.botService = botService;
        this.complianceService = complianceService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking demo data seeding requirement...");

        // Check idempotency: If demo bot "Nifty TrendFollower Pro" exists, skip.
        if (botRepository.findAll().stream().anyMatch(b -> "Nifty TrendFollower Pro".equalsIgnoreCase(b.getName()))) {
            log.info("Demo data already seeded. Skipping DemoDataSeeder execution.");
            return;
        }

        log.info("Starting AlgoAdda Demo Data Seeding (3 Marketplace + 3 Official Store listings)...");

        // 1. Seed Accounts
        User officialSeller = getOrCreateUser(officialSellerEmail, "AlgoAdda Official Labs", "First-party verified quantitative algorithms built by AlgoAdda.");
        User seller1 = getOrCreateUser("alphaquant@algoadda.io", "AlphaQuant Labs", "Institutional quantitative research firm specializing in Nifty futures momentum strategies.");
        User seller2 = getOrCreateUser("deltahedge@algoadda.io", "DeltaHedge Trading", "Options volatility trader with 8 years of systematic options selling experience.");
        User seller3 = getOrCreateUser("banknifty@algoadda.io", "BankNifty Systems", "High-frequency breakout and intraday momentum algorithm developer.");

        // 2. Seed 3 Marketplace Bots (Third-Party)
        seedBotAndPublish(
            seller1,
            "Nifty TrendFollower Pro",
            "Systematic dual EMA crossover algorithm for Nifty 50 index futures with ATR volatility stops.",
            "TREND_FOLLOWING",
            "Calculates 20-period and 50-period Exponential Moving Averages on daily Nifty price series. Enters long when 20 EMA crosses above 50 EMA and ATR is above 20-period median. Exits on reverse crossover or trailing stop at 2x ATR.",
            "Futures trading involves substantial risk of loss. Past performance is not indicative of future results. Manage leverage responsibly.",
            new BigDecimal("149.99"),
            LicenseType.ONE_TIME,
            createMetrics(58.2, 1.85, 14.2, 1.45, 142, 24.5, 100000.0, 124500.0)
        );

        seedBotAndPublish(
            seller2,
            "Weekly Strangle Delta Neutral",
            "Options income strategy selling out-of-the-money weekly index strangles with dynamic delta hedging.",
            "MEAN_REVERSION",
            "Sells 15 delta OTM Nifty options on Monday morning. Dynamically hedges delta when market moves beyond 0.5 standard deviations of implied volatility. Closes positions at 50% max profit or 2x stop loss.",
            "Options selling involves downside exposure during extreme market gaps. Ensure strict margin management. No financial returns guaranteed.",
            new BigDecimal("299.00"),
            LicenseType.TIMED,
            createMetrics(76.4, 2.10, 22.8, 1.62, 98, 31.8, 200000.0, 263600.0)
        );

        seedBotAndPublish(
            seller3,
            "BankNifty Intraday Breakout",
            "Opening range breakout algorithm capturing explosive morning momentum in BankNifty futures.",
            "BREAKOUT",
            "Identifies 15-minute opening range high/low for BankNifty. Enters directionally on a 5-minute candle close beyond the range with 1.5x volume expansion. Exits at 15:15 PM IST or trailing stop.",
            "Intraday momentum strategies experience slippage during news events. Trading carries capital risk. Operate with strict risk limits.",
            new BigDecimal("199.50"),
            LicenseType.ONE_TIME,
            createMetrics(52.0, 1.65, 11.5, 1.28, 210, 18.4, 150000.0, 177600.0)
        );

        // 3. Seed 3 Official Store Bots (AlgoAdda Direct)
        seedBotAndPublish(
            officialSeller,
            "AlgoAdda Flagship Institutional Momentum",
            "Conservative multi-factor momentum model ranking top Nifty 50 stocks by risk-adjusted returns.",
            "MOMENTUM",
            "Multi-factor momentum model ranking top liquid index components by 3-month and 6-month risk-adjusted returns. Rebalances monthly with volatility scaling to target 12% annualized portfolio risk.",
            "Trading in financial instruments involves risk of loss. Historical backtest results are compiled using institutional data and do not guarantee future returns.",
            new BigDecimal("399.00"),
            LicenseType.TIMED,
            createMetrics(64.5, 2.40, 8.6, 2.15, 84, 28.6, 500000.0, 643000.0)
        );

        seedBotAndPublish(
            officialSeller,
            "AlgoAdda High-Alpha Options Scalper",
            "High-frequency index options scalping algorithm capturing intraday micro-trends.",
            "SCALPING",
            "High-frequency momentum scalper capturing micro-bursts in index option greeks during European/US market opening overlap. Uses order book imbalance and tick velocity filters.",
            "High-frequency derivatives trading carries elevated risk and requires low-latency connectivity. Past returns are not indicative of future performance.",
            new BigDecimal("499.00"),
            LicenseType.ONE_TIME,
            createMetrics(71.8, 2.85, 16.4, 2.42, 320, 45.2, 250000.0, 363000.0)
        );

        seedBotAndPublish(
            officialSeller,
            "AlgoAdda Nifty Sector Rotation Engine",
            "Systematic relative strength rotation across IT, Banking, Auto, and Pharma sector indices.",
            "TREND_FOLLOWING",
            "Systematic relative strength rotation across IT, Banking, Auto, and Pharma sector indices. Allocates capital to top 2 performing sectors on weekly momentum triggers with cash filter during market drawdowns.",
            "Sector rotation strategies are subject to whipsaw market cycles. Systematic trading involves financial risk. Manage capital responsibly.",
            new BigDecimal("249.00"),
            LicenseType.TIMED,
            createMetrics(62.0, 2.05, 10.2, 1.78, 52, 22.4, 300000.0, 367200.0)
        );

        log.info("SUCCESS: AlgoAdda Demo Data Seeding completed! 6 total listings created (3 Marketplace, 3 Official Store).");
    }

    private User getOrCreateUser(String email, String displayName, String bio) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("DemoPassword123!"))
                .role(Role.SELLER)
                .build());

            sellerProfileRepository.save(SellerProfile.builder()
                .user(user)
                .displayName(displayName)
                .bio(bio)
                .kycStatus(KycStatus.VERIFIED)
                .build());

            return user;
        });
    }

    private void seedBotAndPublish(
        User seller,
        String name,
        String description,
        String strategyType,
        String disclosedLogic,
        String riskDisclaimer,
        BigDecimal price,
        LicenseType licenseType,
        String metricsJson
    ) {
        log.info("Seeding bot: '{}' for seller '{}'...", name, seller.getEmail());

        BotUploadRequest uploadReq = new BotUploadRequest();
        uploadReq.setName(name);
        uploadReq.setDescription(description);
        uploadReq.setStrategyType(strategyType);
        uploadReq.setDisclosedLogic(disclosedLogic);
        uploadReq.setRiskDisclaimer(riskDisclaimer);

        byte[] pythonCode = ("# Strategy: " + name + "\n"
            + "# Disclosed Logic: " + disclosedLogic + "\n"
            + "import numpy as np\n"
            + "import pandas as pd\n\n"
            + "def execute_strategy(df):\n"
            + "    return df\n").getBytes();

        MultipartFile file = new SimpleInMemoryMultipartFile("file", "strategy.py", "text/x-python", pythonCode);

        var botResponse = botService.createBot(seller.getId(), uploadReq, file);
        UUID botId = botResponse.getId();

        Bot bot = botRepository.findById(botId).orElseThrow();
        BotVersion version = botVersionRepository.findByBotId(botId).get(0);

        // Ensure a completed BacktestResult exists with realistic metrics
        if (backtestResultRepository.findFirstByBotVersionIdOrderByCreatedAtDesc(version.getId()).isEmpty()) {
            BacktestResult backtestResult = BacktestResult.builder()
                .botVersion(version)
                .dateRangeStart(Instant.now().minus(365, ChronoUnit.DAYS))
                .dateRangeEnd(Instant.now())
                .methodologyNotes("Verified 1-year historical backtest executed via AlgoAdda Engine.")
                .metrics(metricsJson)
                .build();
            backtestResultRepository.save(backtestResult);

            version.setBacktestStatus(BacktestStatus.COMPLETED);
            botVersionRepository.save(version);
        }

        // Run automated compliance check (will pass 100%)
        var complianceCheck = complianceService.runAutomatedComplianceCheck(version);
        if (!complianceCheck.isPassed()) {
            throw new IllegalStateException("Unexpected compliance failure during demo seeding for bot: " + name);
        }

        // Publish bot version
        PublishRequest pubReq = new PublishRequest();
        pubReq.setPrice(price);
        pubReq.setLicenseType(licenseType);

        PublishResponse pubRes = botService.publishBotVersion(seller.getId(), botId, version.getId(), pubReq);
        log.info("Published listing ID: {} (isOfficial={}) for bot '{}'", pubRes.getListingId(), pubRes.isOfficial(), name);
    }

    private String createMetrics(
        double winRate,
        double profitFactor,
        double maxDrawdown,
        double sharpeRatio,
        int totalTrades,
        double totalReturnPct,
        double startingCapital,
        double endingCapital
    ) {
        try {
            Map<String, Object> metricsMap = Map.of(
                "win_rate", winRate,
                "profit_factor", profitFactor,
                "max_drawdown", maxDrawdown,
                "sharpe_ratio", sharpeRatio,
                "total_trades", totalTrades,
                "total_return_pct", totalReturnPct,
                "starting_capital", startingCapital,
                "ending_capital", endingCapital
            );
            return objectMapper.writeValueAsString(metricsMap);
        } catch (Exception e) {
            return "{\"win_rate\":" + winRate + ",\"max_drawdown\":" + maxDrawdown + ",\"sharpe_ratio\":" + sharpeRatio + "}";
        }
    }

    private static class SimpleInMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public SimpleInMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content != null ? content : new byte[0];
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), content);
        }
    }
}
