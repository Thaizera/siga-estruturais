package siga;

import java.util.Objects;

public class RelatorioSituacao {
    private final FonteDeAlunos fonte;

    public RelatorioSituacao(FonteDeAlunos fonte) {
        this.fonte = Objects.requireNonNull(fonte);
    }

    public void imprimir() {
        System.out.println("   Situacao segundo o relatorio:");
        for (Aluno aluno : fonte.listar()) {
            System.out.println("     " + aluno);
        }
    }
}
