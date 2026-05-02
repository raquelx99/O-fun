package ui;

import java.io.File;

import benchmark.CsvExporter;
import benchmark.CsvResumoExporter;

public class CsvUiInitializer {

    public static void inicializar(String csvBruto, String csvResumo) {
        File arquivoBruto = new File(csvBruto);
        File arquivoResumo = new File(csvResumo);

        if (!arquivoBruto.exists()) {
            CsvExporter.writeHeader(csvBruto);
        }

        if (!arquivoResumo.exists()) {
            CsvResumoExporter.writeHeader(csvResumo);
        }
    }
}