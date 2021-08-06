package com.example.richard.ectablet.Clases;

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

}
