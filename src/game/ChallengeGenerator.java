package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.TipoEntrada;

public class ChallengeGenerator {

    private List<ChallengeScenario> scenarios;
    private Random random;

    public ChallengeGenerator() {
        random = new Random();
        scenarios = new ArrayList<>();

        criarCenarios();
    }

    private void criarCenarios() {
        scenarios.add(new ChallengeScenario(
                "Horda Aleatória",
                "Uma horda de inimigos surgiu de forma imprevisível. Ordene as ameaças rapidamente.",
                5000,
                TipoEntrada.ALEATORIA
        ));

        scenarios.add(new ChallengeScenario(
                "Ranking Quase Ordenado",
                "O ranking dos jogadores já está quase correto, mas algumas posições mudaram após a última partida.",
                10000,
                TipoEntrada.QUASE_ORDENADA
        ));

        scenarios.add(new ChallengeScenario(
                "Fila Invertida",
                "A fila de prioridade foi carregada ao contrário. Os elementos estão na ordem menos favorável.",
                5000,
                TipoEntrada.INVERTIDA
        ));

        scenarios.add(new ChallengeScenario(
                "Inventário Organizado",
                "O inventário já está ordenado. O desafio é escolher um algoritmo que lide bem com esse caso.",
                10000,
                TipoEntrada.ORDENADA
        ));

        scenarios.add(new ChallengeScenario(
                "Prioridades Repetidas",
                "Muitos inimigos têm o mesmo nível de ameaça. A entrada possui muitos valores repetidos.",
                10000,
                TipoEntrada.REPETIDA
        ));
    }

    public ChallengeScenario sortearCenario() {
        int index = random.nextInt(scenarios.size());
        return scenarios.get(index);
    }
}