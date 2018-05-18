package com.trade;


import java.util.*;
import java.text.*;
import java.text.DecimalFormat;
import java.time.LocalDate; 
import java.time.format.*;
import java.time.LocalDateTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author anto 
 * USD Daily trade  technical exercise JP Morgan
 * 
 */     
public class USDSettlements {
    //use a HashMap to sum up settlements with the date as the key.
    public static Map<String, Double> USD_settled_incoming = new HashMap<String,Double>();
    public static Map<String,Double> USD_settled_outgoing = new HashMap<String,Double>();
    public static Map<String,Double> USD_settled_ranking = new HashMap<String,Double>();
    public static DecimalFormat df = new DecimalFormat("####0.00");
    //this method parses the string and then returns the USD amount of trade.
    public void tradeAmount(String ticker){
        String[] parts = ticker.split("_");
        double Price_per_unit = Double.parseDouble(parts[7]);
        double Units = Double.parseDouble(parts[6]);
        double Agreed_FX = Double.parseDouble(parts[2]);
        double USD_amount_trade = Price_per_unit * Units * Agreed_FX;
        String original_Settlement_Date=parts[5];
        String entity_name = parts[0];
        String buy_or_sell = parts[1];
        String currency_type = parts[3];
        String dateInString = parts[5];
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Date formatted_SettlementDate = new Date();
        
        try{ 
           formatted_SettlementDate = dateFormat.parse(dateInString);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(formatted_SettlementDate);
        System.out.println("entity is "+entity_name +"formatted_SettlementDay was "+formatted_SettlementDate);

        if(currency_type.equals("AED") || currency_type.equals("SAR")){
            //the settlement can only be SUNDAY to THURSDAY
            if(formatted_SettlementDate.toString().startsWith("Fri")){ 
                        c.add(Calendar.DATE, 2);  
            }else if(formatted_SettlementDate.toString().startsWith("Sat")){
                        c.add(Calendar.DATE, 1);  
            } 
        }else{
            //otherwise settlement must be between Monday to Friday
            if(formatted_SettlementDate.toString().startsWith("Sat")){
                        c.add(Calendar.DATE, 2);  
            }else if(formatted_SettlementDate.toString().startsWith("Sun")){
                        c.add(Calendar.DATE, 1);  
            }
        }
        Date currentDatePlusOne = c.getTime();
        
        double existing_USD_amount_trade=0.00;
        //B  Buy outgoing
        //S  Sell incoming
        if(buy_or_sell.equals("B")){
            //add to outgoing settlement
            if(USD_settled_outgoing.containsKey(dateFormat.format(currentDatePlusOne).toString())){
                    existing_USD_amount_trade=USD_settled_outgoing.get(dateFormat.format(currentDatePlusOne).toString());
                    USD_settled_outgoing.put(dateFormat.format(currentDatePlusOne).toString(), USD_amount_trade+existing_USD_amount_trade);
                 }else{
                    USD_settled_outgoing.put(dateFormat.format(currentDatePlusOne).toString(), USD_amount_trade);
            }
            USD_settled_ranking.put(entity_name,  USD_amount_trade);
            existing_USD_amount_trade=0.00;
        }else if(buy_or_sell.equals("S")){
            //otherwise add to incoming settlement
            if(USD_settled_incoming.containsKey(dateFormat.format(currentDatePlusOne).toString())){
                existing_USD_amount_trade=USD_settled_incoming.get(dateFormat.format(currentDatePlusOne).toString());
                USD_settled_incoming.put(dateFormat.format(currentDatePlusOne).toString(), USD_amount_trade+existing_USD_amount_trade);
             }else{
                USD_settled_incoming.put(dateFormat.format(currentDatePlusOne).toString(), USD_amount_trade);
            }
            USD_settled_ranking.put(entity_name,  USD_amount_trade);
            existing_USD_amount_trade=0.00;
        }
    }
    
    private static String getDayName(int dayofWeek) {
		String dayName = null;
		switch (dayofWeek) {
		case 1:
			dayName = "Sunday";
			break;
		case 2:
			dayName = "Monday";
			break;
		case 3:
			dayName = "Tuesday";
			break;
		case 4:
			dayName = "Wednesday";
			break;
		case 5:
			dayName = "Thursday";
			break;
		case 6:
			dayName = "Friday";
			break;
		case 7:
			dayName = "Saturday";
			break;
		}
		return dayName;
	}

    public static void main(String args []){
      //Entity Buy/Sell AgreedFx Currency InstructionDate SettlementDate Units Price_per_unit
        String test1 = "foo_B_0.50_SGP_01 Jan 2016_02 Jan 2016_200_100.25";
        String test2 = "bar_S_0.22_AED_05 Jan 2016_07 Jan 2016_450_150.50";
        String test3 = "boo_S_0.33_SAR_10 Jan 2016_07 Jan 2016_500_180.50";
        String test4 = "moo_B_0.48_ZAR_15 Jan 2016_20 Jan 2016_550_190.80";
        String test5 = "zoo_S_0.54_CHF_20 Jan 2016_25 Jan 2016_300_200.23";
        
        USDSettlements a = new USDSettlements();
        a.tradeAmount(test1);
        a.tradeAmount(test2);
        a.tradeAmount(test3);
        a.tradeAmount(test4);
        a.tradeAmount(test5);
        
        System.out.println("### Outgoing USD Settlements ###");
        for (Map.Entry<String, Double> entry : USD_settled_outgoing.entrySet()) {
            String date = entry.getKey();
            Double value = entry.getValue();
            System.out.println("Date is "+date+" and amount is $"+df.format(value));
        }
        System.out.println("### End of Outgoing USD Settlements ###");
            System.out.println(" ");
            System.out.println("### Incoming USD Settlements ###");
        for (Map.Entry<String, Double> entry : USD_settled_incoming.entrySet()) {
            String date = entry.getKey();
            Double value = entry.getValue();
            System.out.println("Date is "+date+" and amount is $"+df.format(value));
        }
        System.out.println("### End of Incoming USD Settlements ###");
        //print the rank by amount of highest settlement.
        sortMapByValues(USD_settled_ranking);
        
    }   
    
        private static void sortMapByValues(Map<String, Double> aMap) {        
            Set<Entry<String,Double>> mapEntries = aMap.entrySet();

            // used linked list to sort, because insertion of elements in linked list is faster than an array list. 
            List<Entry<String,Double>> aList = new LinkedList<Entry<String,Double>>(mapEntries);

            // sorting the List
            Collections.sort(aList, new Comparator<Entry<String,Double>>() {
                @Override
                public int compare(Entry<String, Double> ele1,
                        Entry<String, Double> ele2) {
                    return ele2.getValue().compareTo(ele1.getValue());
                }
            });

            // Storing the list into Linked HashMap to preserve the order of insertion. 
            Map<String,Double> aMap2 = new LinkedHashMap<String, Double>();
            for(Entry<String,Double> entry: aList) {
                aMap2.put(entry.getKey(), entry.getValue());
            }

            // printing values after sorting of map
            System.out.println("         ");
            System.out.println("### Ranking of USD Settlements by descending order ###            ");
            for(Entry<String,Double> entry : aMap2.entrySet()) {
                System.out.println(entry.getKey()+" and amount is $"+df.format(entry.getValue()));
            }   
            System.out.println("### End of Ranking of USD Settlements ###            ");
    }
}       
