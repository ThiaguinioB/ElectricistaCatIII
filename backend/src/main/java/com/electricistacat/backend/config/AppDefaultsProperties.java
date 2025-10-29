package com.electricistacat.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppDefaultsProperties {

    private VoltageDrop voltageDrop = new VoltageDrop();
    private Defaults defaults = new Defaults();

    public VoltageDrop getVoltageDrop() {
        return voltageDrop;
    }

    public Defaults getDefault() {
        return defaults;
    }

    public static class VoltageDrop {
        private double lightingLimitPercent;
        private double generalLimitPercent;

        public double getLightingLimitPercent() {
            return lightingLimitPercent;
        }

        public void setLightingLimitPercent(double lightingLimitPercent) {
            this.lightingLimitPercent = lightingLimitPercent;
        }

        public double getGeneralLimitPercent() {
            return generalLimitPercent;
        }

        public void setGeneralLimitPercent(double generalLimitPercent) {
            this.generalLimitPercent = generalLimitPercent;
        }
    }

    public static class Defaults {
        private double voltageSinglePhase;
        private double voltageThreePhase;
        private double cosphi;
        private double efficiency;
        private String conductorMaterial;
        private String installationMethod;

        public double getVoltageSinglePhase() {
            return voltageSinglePhase;
        }

        public void setVoltageSinglePhase(double voltageSinglePhase) {
            this.voltageSinglePhase = voltageSinglePhase;
        }

        public double getVoltageThreePhase() {
            return voltageThreePhase;
        }

        public void setVoltageThreePhase(double voltageThreePhase) {
            this.voltageThreePhase = voltageThreePhase;
        }

        public double getCosphi() {
            return cosphi;
        }

        public void setCosphi(double cosphi) {
            this.cosphi = cosphi;
        }

        public double getEfficiency() {
            return efficiency;
        }

        public void setEfficiency(double efficiency) {
            this.efficiency = efficiency;
        }

        public String getConductorMaterial() {
            return conductorMaterial;
        }

        public void setConductorMaterial(String conductorMaterial) {
            this.conductorMaterial = conductorMaterial;
        }

        public String getInstallationMethod() {
            return installationMethod;
        }

        public void setInstallationMethod(String installationMethod) {
            this.installationMethod = installationMethod;
        }
    }
}
