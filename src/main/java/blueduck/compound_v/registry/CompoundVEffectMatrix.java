package blueduck.compound_v.registry;

import blueduck.compound_v.util.CompoundVEffectGiver;

import java.util.ArrayList;

public class CompoundVEffectMatrix {

    public static ArrayList<CompoundVEffectGiver> EFFECT_MATRIX = new ArrayList<>();

    public static ArrayList<CompoundVEffectGiver> FAILURE_MATRIX = new ArrayList<>();

    // Mob-injectable pools: only powers that mechanically function on non-player entities
    public static ArrayList<CompoundVEffectGiver> MOB_EFFECT_MATRIX = new ArrayList<>();

    public static ArrayList<CompoundVEffectGiver> MOB_FAILURE_MATRIX = new ArrayList<>();

    // V1 pool: curated list of powerful effects for the original formula
    public static ArrayList<CompoundVEffectGiver> V1_EFFECT_MATRIX = new ArrayList<>();

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

    public static void addMobEffect(CompoundVEffectGiver effect, int weight) {
        for (int i = 0; i < weight; i++) {
            MOB_EFFECT_MATRIX.add(effect);
        }
    }

    public static void addMobFailureEffect(CompoundVEffectGiver effect, int weight) {
        for (int i = 0; i < weight; i++) {
            MOB_FAILURE_MATRIX.add(effect);
        }
    }

    public static void addV1Effect(CompoundVEffectGiver effect, int weight) {
        for (int i = 0; i < weight; i++) {
            V1_EFFECT_MATRIX.add(effect);
        }
    }

    /**
     * Find the maximum amplifier level for a given MobEffect across all matrices.
     * Returns -1 if not found.
     */
    public static int getMaxLevel(net.minecraft.world.effect.MobEffect effect) {
        int max = -1;
        for (var matrix : new ArrayList[]{EFFECT_MATRIX, FAILURE_MATRIX, MOB_EFFECT_MATRIX, MOB_FAILURE_MATRIX, V1_EFFECT_MATRIX}) {
            for (Object obj : matrix) {
                CompoundVEffectGiver giver = (CompoundVEffectGiver) obj;
                if (giver.mobEffect == effect && giver.levels > max) {
                    max = giver.levels;
                }
            }
        }
        return max;
    }
}
