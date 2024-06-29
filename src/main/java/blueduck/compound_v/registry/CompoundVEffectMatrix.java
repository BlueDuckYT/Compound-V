package blueduck.compound_v.registry;

import blueduck.compound_v.util.CompoundVEffectGiver;

import java.util.ArrayList;

public class CompoundVEffectMatrix {

    public static ArrayList<CompoundVEffectGiver> EFFECT_MATRIX = new ArrayList<>();

    public static ArrayList<CompoundVEffectGiver> FAILURE_MATRIX = new ArrayList<>();


    public static void addEffect(CompoundVEffectGiver effect, int weight) {
        for (int i = 0; i < weight; i++) {
            EFFECT_MATRIX.add(effect);
        }
    }

    public static void addFailureEffect(CompoundVEffectGiver effect, int weight) {
        for (int i = 0; i < weight; i++) {
            FAILURE_MATRIX.add(effect);
        }
    }

}
