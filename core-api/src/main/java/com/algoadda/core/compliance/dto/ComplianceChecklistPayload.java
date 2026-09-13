package com.algoadda.core.compliance.dto;

public class ComplianceChecklistPayload {
    private CheckItemResult disclosedLogicPresent;
    private CheckItemResult noGuaranteedReturnLanguage;
    private CheckItemResult riskDisclaimerPresent;
    private CheckItemResult backtestCompleted;

    public ComplianceChecklistPayload() {
    }

    public ComplianceChecklistPayload(
        CheckItemResult disclosedLogicPresent,
        CheckItemResult noGuaranteedReturnLanguage,
        CheckItemResult riskDisclaimerPresent,
        CheckItemResult backtestCompleted
    ) {
        this.disclosedLogicPresent = disclosedLogicPresent;
        this.noGuaranteedReturnLanguage = noGuaranteedReturnLanguage;
        this.riskDisclaimerPresent = riskDisclaimerPresent;
        this.backtestCompleted = backtestCompleted;
    }

    public CheckItemResult getDisclosedLogicPresent() {
        return disclosedLogicPresent;
    }

    public void setDisclosedLogicPresent(CheckItemResult disclosedLogicPresent) {
        this.disclosedLogicPresent = disclosedLogicPresent;
    }

    public CheckItemResult getNoGuaranteedReturnLanguage() {
        return noGuaranteedReturnLanguage;
    }

    public void setNoGuaranteedReturnLanguage(CheckItemResult noGuaranteedReturnLanguage) {
        this.noGuaranteedReturnLanguage = noGuaranteedReturnLanguage;
    }

    public CheckItemResult getRiskDisclaimerPresent() {
        return riskDisclaimerPresent;
    }

    public void setRiskDisclaimerPresent(CheckItemResult riskDisclaimerPresent) {
        this.riskDisclaimerPresent = riskDisclaimerPresent;
    }

    public CheckItemResult getBacktestCompleted() {
        return backtestCompleted;
    }

    public void setBacktestCompleted(CheckItemResult backtestCompleted) {
        this.backtestCompleted = backtestCompleted;
    }
}
