package siga;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestesSiga {
    private static int verificacoes;

    private static void verificar(boolean condicao, String descricao) {
        verificacoes++;
        if (!condicao) {
            throw new AssertionError(descricao);
        }
    }

    private static void esperarFalha(Runnable acao, String mensagem) {
        try {
            acao.run();
            throw new AssertionError("Falha esperada: " + mensagem);
        } catch (IllegalArgumentException | IllegalStateException erro) {
            verificar(erro.getMessage().contains(mensagem), "Mensagem incorreta: " + erro);
        }
    }

    public static void main(String[] args) {
        FonteDeAlunos fonte = new AdaptadorSecretariaLegado(new SecretariaLegadoWS());
        List<Aluno> alunos = fonte.listar();
        verificar(alunos.size() == 4, "Quantidade adaptada incorreta");
        verificar(alunos.get(0).isAtivo(), "Situacao A incorreta");
        verificar(!alunos.get(1).isAtivo(), "Situacao I incorreta");
        verificar(alunos.get(2).isAtivo(), "Situacao a incorreta");
        verificar(alunos.get(2).getMatricula().equals("2024003"), "Matricula convertida incorreta");
        verificar(alunos.get(2).getNome().equals("Ana Pereira"), "Nome convertido incorreto");
        AlunoDAO dao = new AlunoDAOMemoria();
        verificar(new ImportadorAlunos(fonte, dao).importar() == 4, "Quantidade importada incorreta");
        verificar(dao.buscarPorMatricula("2024003").isAtivo(), "Ana inativa apos importacao");
        String relatorio = capturar(() -> new RelatorioSituacao(fonte).imprimir());
        verificar(relatorio.contains("2024003 - Ana Pereira (ativo)"), "Relatorio divergente");
        alunos.clear();
        verificar(fonte.listar().size() == 4, "Lista anterior afetou nova consulta");

        Aluno teste = new Aluno("TESTE", "Fonte alternativa");
        FonteDeAlunos alternativa = () -> List.of(teste);
        AlunoDAO outroDao = new AlunoDAOMemoria();
        verificar(new ImportadorAlunos(alternativa, outroDao).importar() == 1,
                "Importador nao aceita fonte substituta");
        verificar(outroDao.buscarPorMatricula("TESTE") == teste, "Fonte alternativa nao importada");
        verificar(capturar(() -> new RelatorioSituacao(alternativa).imprimir()).contains("Fonte alternativa"),
                "Relatorio nao aceita fonte substituta");
        verificar(new ImportadorAlunos(List::of, new AlunoDAOMemoria()).importar() == 0,
                "Fonte vazia incorreta");

        ControleVagas vagas = new ControleVagas();
        MatriculaDAO matriculas = new MatriculaDAOMemoria();
        List<String> eventos = new ArrayList<>();
        MatriculaDAO daoObservado = new MatriculaDAO() {
            public void inserir(Matricula matricula) {
                eventos.add("gravar");
                matriculas.inserir(matricula);
            }
            public List<Matricula> listarTodas() {
                return matriculas.listarTodas();
            }
        };
        ControleVagas vagasObservadas = new ControleVagas() {
            @Override
            public boolean haVaga(String turma) {
                return vagas.haVaga(turma);
            }
            @Override
            public void reservar(String turma) {
                eventos.add("reservar");
                vagas.reservar(turma);
            }
        };
        NotificadorEmail notificador = new NotificadorEmail() {
            @Override
            public void enviarConfirmacao(Aluno aluno, Matricula matricula) {
                eventos.add("notificar");
                verificar(matriculas.listarTodas().contains(matricula), "Notificacao antes da gravacao");
            }
        };
        FachadaMatricula fachada = new FachadaMatricula(new ValidadorCpf(), dao,
                vagasObservadas, new CalculadoraDesconto(), daoObservado, notificador);
        esperarFalha(() -> fachada.matricular("2024001", "111", "DSM-2A"), "CPF invalido");
        esperarFalha(() -> fachada.matricular("2024001", null, "DSM-2A"), "CPF invalido");
        esperarFalha(() -> fachada.matricular("ausente", "123.456.789-09", "DSM-2A"), "Aluno nao encontrado");
        esperarFalha(() -> fachada.matricular("2024002", "123.456.789-09", "DSM-2A"), "Aluno inativo");
        esperarFalha(() -> fachada.matricular("2024004", "123.456.789-09", "DSM-2B"), "Turma sem vagas");
        esperarFalha(() -> fachada.matricular("2024004", "123.456.789-09", "DESCONHECIDA"), "Turma sem vagas");
        verificar(eventos.isEmpty(), "Validacao falha gerou efeito colateral");
        verificar(matriculas.listarTodas().isEmpty(), "Validacao falha gravou matricula");
        dao.buscarPorMatricula("2024001").setBolsista(true);
        Matricula maria = fachada.matricular("2024001", "123.456.789-09", "DSM-2A");
        verificar(maria.getDesconto() == 0.5, "Desconto de bolsista incorreto");
        verificar(maria.getAluno().getMatricula().equals("2024001"), "Aluno incorreto na matricula");
        verificar(maria.getCodigoTurma().equals("DSM-2A"), "Turma incorreta");
        verificar(eventos.equals(List.of("gravar", "reservar", "notificar")), "Ordem de operacoes incorreta");
        verificar(vagas.haVaga("DSM-2A"), "Primeira matricula consumiu todas as vagas");
        eventos.clear();
        Matricula ana = fachada.matricular("2024003", "123.456.789-09", "DSM-2A");
        verificar(ana.getDesconto() == 0, "Desconto regular incorreto");
        verificar(!vagas.haVaga("DSM-2A"), "Vagas nao esgotaram");
        eventos.clear();
        esperarFalha(() -> fachada.matricular("2024004", "123.456.789-09", "DSM-2A"), "Turma sem vagas");
        verificar(eventos.isEmpty(), "Turma cheia gerou efeitos");
        verificar(matriculas.listarTodas().size() == 2, "Quantidade de matriculas incorreta");
        String tela = capturar(() -> new TelaMatricula(fachada)
                .aoClicarEmMatricular("2024004", "123.456.789-09", "DSM-2A"));
        verificar(tela.contains("[tela] Erro:"), "Tela nao tratou falha");
        verificar(!tela.contains("Matricula realizada"), "Tela informou sucesso indevido");
        String principal = capturar(() -> Main.main(new String[0]));
        verificar(principal.contains("Matriculas gravadas: 2"), "Demonstracao nao concluiu");
        verificar(principal.contains("desconto de 50%"), "Demonstracao perdeu desconto");
        System.out.println("OK: " + verificacoes + " verificacoes de Adapter, fontes alternativas, "
                + "Facade, validacoes, vagas, desconto, ordem e apresentacao.");
    }

    private static String capturar(Runnable acao) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
            acao.run();
        } finally {
            System.setOut(original);
        }
        return saida.toString(StandardCharsets.UTF_8);
    }
}
