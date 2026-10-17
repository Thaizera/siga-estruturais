package siga;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SIGA - Aula 10: Adapter e Facade ===");
        FonteDeAlunos fonte = new AdaptadorSecretariaLegado(new SecretariaLegadoWS());
        AlunoDAO alunoDAO = new AlunoDAOMemoria();
        ImportadorAlunos importador = new ImportadorAlunos(fonte, alunoDAO);
        System.out.println("\n1) Importacao a partir da fonte adaptada");
        System.out.println("   Alunos importados: " + importador.importar());
        for (Aluno aluno : alunoDAO.listarTodos()) {
            System.out.println("     " + aluno);
        }
        System.out.println("\n2) Relatorio pela mesma fonte");
        new RelatorioSituacao(fonte).imprimir();

        alunoDAO.buscarPorMatricula("2024001").setBolsista(true);
        MatriculaDAO matriculaDAO = new MatriculaDAOMemoria();
        FachadaMatricula fachada = new FachadaMatricula(new ValidadorCpf(), alunoDAO,
                new ControleVagas(), new CalculadoraDesconto(), matriculaDAO,
                new NotificadorEmail());
        TelaMatricula tela = new TelaMatricula(fachada);
        System.out.println("\n3) Cenarios originais de matricula");
        tela.aoClicarEmMatricular("2024001", "123.456.789-09", "DSM-2A");
        tela.aoClicarEmMatricular("2024002", "111", "DSM-2A");
        tela.aoClicarEmMatricular("2024009", "123.456.789-09", "DSM-2A");
        tela.aoClicarEmMatricular("2024004", "123.456.789-09", "DSM-2B");
        System.out.println("\n4) Cenarios adicionais do guia");
        tela.aoClicarEmMatricular("2024002", "123.456.789-09", "DSM-2A");
        tela.aoClicarEmMatricular("2024003", "123.456.789-09", "DSM-2A");
        tela.aoClicarEmMatricular("2024004", "123.456.789-09", "DSM-2A");
        System.out.println("\nMatriculas gravadas: " + matriculaDAO.listarTodas().size());
    }
}
