package trans.strategy;

import javafx.util.Pair;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;

public class StrategyOdevity extends Strategy{

    private Pair<String, Double> checkOdevity(String def, String figaroID, String str) {
        try {
            int val = Integer.parseInt(str);

            if (def.endsWith("odd") || def.endsWith("Odd"))
                if (val % 2 == 0) return new Pair<>(figaroID, 0.2);
                else return new Pair<>(figaroID, 0.8);

            if (def.endsWith("even") || def.endsWith("Even"))
                if (val % 2 == 0) return new Pair<>(figaroID, 0.8);
                else return new Pair<>(figaroID, 0.2);

            return null;
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public ArrayList<Pair<String, Double>> parse(ArrayList<DynamicMsg> msgList) {
        ArrayList<Pair<String, Double>> observationList = new ArrayList<>();
        for (DynamicMsg msg : msgList)
            if (msg instanceof DynamicAssign)
            {
                DynamicAssign ass = (DynamicAssign) msg;
                Pair<String, Double> ret = checkOdevity(ass.getDefVar().getID(), ass.getFigaroID(), ass.getValue());
                if (ret != null) observationList.add(ret);
            }
        return observationList;
    }
}
