package com.example.richard.ectablet.Clases;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BatteryEqtns {

    public int CAPACIDAD_NOMINAL = 0;
    public int KWH = 0;

    public double getCapacidadDisponible(double _soc){
        return (_soc * CAPACIDAD_NOMINAL) / 100; //return in Ah
    }

    public double getEnergiaDisponible(){
        double cd1 = 96 * 3.5 * 15.21;
        return cd1 * (3/1000);
    }

    public double getDTE(int _km){
        return 8.8 * (_km / KWH) * 28;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
