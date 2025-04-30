package com.bd.sitebd.model;
import java.util.Map;

// Conversão de dados
public class Tool {
    
    public static Reserva converterReserva(Map<String, Object> registro) { //chave:String , valor:Object(qualquer tipo).
// Como registro.get retorna Object, devemos usar o polimorfismo de subtipos (downcast) para recuperar os tipos originais.
        return new Reserva((Integer) registro.get("id"), 
                        (String) registro.get("numero"), 
                        (String) registro.get("nome"),
                        ((java.sql.Date) registro.get("data")).toLocalDate(), // converto o que veio em object e mostro pro java que na vdd é um java.sql.date(interage com bd) e converte p toLocalDate() 
                        ((java.sql.Time) registro.get("hora")).toLocalTime(), 
                        (Integer) registro.get("duracao"));
    }
}
