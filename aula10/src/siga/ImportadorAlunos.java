package siga;

import java.util.Objects;

public class ImportadorAlunos {
    private final FonteDeAlunos fonte;
    private final AlunoDAO dao;

    public ImportadorAlunos(FonteDeAlunos fonte, AlunoDAO dao) {
        this.fonte = Objects.requireNonNull(fonte);
        this.dao = Objects.requireNonNull(dao);
    }

    public int importar() {
        int importados = 0;
        for (Aluno aluno : fonte.listar()) {
            dao.inserir(aluno);
            importados++;
        }
        return importados;
    }
}
