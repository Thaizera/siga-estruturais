package siga;

import java.util.Objects;

/** Coordena a sequência do caso de uso de matrícula. */
public class FachadaMatricula {
    private final ValidadorCpf validadorCpf;
    private final AlunoDAO alunoDAO;
    private final ControleVagas controleVagas;
    private final CalculadoraDesconto calculadoraDesconto;
    private final MatriculaDAO matriculaDAO;
    private final NotificadorEmail notificador;

    public FachadaMatricula(ValidadorCpf validadorCpf, AlunoDAO alunoDAO,
            ControleVagas controleVagas, CalculadoraDesconto calculadoraDesconto,
            MatriculaDAO matriculaDAO, NotificadorEmail notificador) {
        this.validadorCpf = Objects.requireNonNull(validadorCpf);
        this.alunoDAO = Objects.requireNonNull(alunoDAO);
        this.controleVagas = Objects.requireNonNull(controleVagas);
        this.calculadoraDesconto = Objects.requireNonNull(calculadoraDesconto);
        this.matriculaDAO = Objects.requireNonNull(matriculaDAO);
        this.notificador = Objects.requireNonNull(notificador);
    }

    public Matricula matricular(String matricula, String cpf, String codigoTurma) {
        if (!validadorCpf.valido(cpf)) {
            throw new IllegalArgumentException("CPF invalido.");
        }
        Aluno aluno = alunoDAO.buscarPorMatricula(matricula);
        if (aluno == null) {
            throw new IllegalStateException("Aluno nao encontrado: " + matricula);
        }
        if (!aluno.isAtivo()) {
            throw new IllegalStateException("Aluno inativo nao pode ser matriculado: " + matricula);
        }
        if (!controleVagas.haVaga(codigoTurma)) {
            throw new IllegalStateException("Turma sem vagas: " + codigoTurma);
        }
        double desconto = calculadoraDesconto.calcular(aluno);
        Matricula nova = new Matricula(aluno, codigoTurma, desconto);
        matriculaDAO.inserir(nova);
        controleVagas.reservar(codigoTurma);
        notificador.enviarConfirmacao(aluno, nova);
        return nova;
    }
}
