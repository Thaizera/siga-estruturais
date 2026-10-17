package siga;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Único ponto que conhece as colunas e a situação textual do legado. */
public class AdaptadorSecretariaLegado implements FonteDeAlunos {
    private final SecretariaLegadoWS servicoLegado;

    public AdaptadorSecretariaLegado(SecretariaLegadoWS servicoLegado) {
        this.servicoLegado = Objects.requireNonNull(servicoLegado);
    }

    @Override
    public List<Aluno> listar() {
        List<Aluno> alunos = new ArrayList<>();
        for (String[] linha : servicoLegado.consultarTabelaAlunos()) {
            Aluno aluno = new Aluno(linha[0], linha[1]);
            aluno.setAtivo("A".equalsIgnoreCase(linha[2]));
            alunos.add(aluno);
        }
        return alunos;
    }
}
