package siga;

import java.util.Objects;

/** Apresentação: delega o caso de uso e exibe resultado ou erro. */
public class TelaMatricula {
    private final FachadaMatricula fachada;

    public TelaMatricula(FachadaMatricula fachada) {
        this.fachada = Objects.requireNonNull(fachada);
    }

    public void aoClicarEmMatricular(String matricula, String cpf, String codigoTurma) {
        try {
            Matricula nova = fachada.matricular(matricula, cpf, codigoTurma);
            System.out.println("   [tela] Matricula realizada: " + nova);
        } catch (IllegalArgumentException | IllegalStateException erro) {
            System.out.println("   [tela] Erro: " + erro.getMessage());
        }
    }
}
