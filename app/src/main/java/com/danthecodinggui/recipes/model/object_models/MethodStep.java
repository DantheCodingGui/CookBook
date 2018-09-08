package com.danthecodinggui.recipes.model.object_models;

public class MethodStep {

    private String stepText;
    private int stepNumber;

    public MethodStep(String stepText, int stepNumber) {
        this.stepText = stepText;
        this.stepNumber = stepNumber;
    }

    public String getStepText() {
        return stepText;
    }

    public void setStepText(String stepText) {
        this.stepText = stepText;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }
}
