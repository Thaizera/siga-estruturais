# Aula 10 — Adapter e Facade

## Problemas encontrados

ImportadorAlunos e RelatorioSituacao convertiam a mesma matriz do legado.
O importador usava uma comparação sensível a maiúsculas e o relatório ignorava
a caixa da letra. Assim, Ana Pereira (2024003), cuja situação é `a`, aparecia
inativa na importação e ativa no relatório. O importador também dependia
diretamente da classe externa, dificultando trocar e testar a origem dos dados.

TelaMatricula recebia seis colaboradores e coordenava validação, busca,
desconto, gravação, reserva de vaga e notificação. A apresentação conhecia
uma sequência de negócio que precisaria ser repetida em outro cliente.

## Soluções e decisões

**Adapter:** FonteDeAlunos oferece `List<Aluno> listar()`.
AdaptadorSecretariaLegado recebe o serviço externo por composição e concentra
o conhecimento das três colunas e a comparação sem distinção de caixa.
ImportadorAlunos e RelatorioSituacao consomem o mesmo contrato, sem conhecer
matrizes ou códigos de situação. Os testes usam uma fonte alternativa sem legado.
SecretariaLegadoWS foi preservada byte a byte, assim como os componentes prontos.

**Facade:** FachadaMatricula concentra os seis colaboradores e oferece
`matricular(String matricula, String cpf, String codigoTurma)`. Ela valida,
busca o aluno, calcula o desconto, grava, reserva a vaga e notifica nessa ordem.
O retorno Matricula permite à tela apresentar o mesmo resultado detalhado.
TelaMatricula tem apenas um atributo colaborador, chama a fachada e trata
os erros esperados, sem repetir regras ou acessar DAOs.

Os quatro cenários originais permanecem: sucesso da bolsista, CPF inválido,
aluno ausente e turma sem vagas. O guia de resolução também especifica a
recusa de aluno inativo, que foi incluída e demonstrada separadamente.
Ana permanece ativa na importação e no relatório e pode se matricular.

O guia cita `isValido`, mas o componente pronto fornece `valido`; foi usado
o método existente. O validador fornecido verifica apenas a quantidade de
dígitos, sem cálculo de dígitos verificadores. Essa regra foi preservada.

Esta implementação didática usa memória e execução sequencial. Não inclui
transação ou compensação entre gravação, reserva e notificação; uma falha
inesperada após gravar pode deixar efeitos parciais, como no subsistema original.
Importar novamente no mesmo DAO gera erro de matrícula duplicada.

## Compilar, executar e testar

Pré-requisito: JDK 17 ou superior. Em PowerShell, dentro de aula10:

```powershell
New-Item -ItemType Directory -Force bin | Out-Null
javac -encoding UTF-8 --release 17 -d bin src/siga/*.java
java -cp bin siga.Main
javac -encoding UTF-8 --release 17 -cp bin -d bin tests/siga/TestesSiga.java
java -cp bin siga.TestesSiga
```

Os testes verificam conversão de situação, consistência das saídas, fonte
alternativa, validações sem efeitos colaterais, desconto, consumo de vagas,
ordem gravar → reservar → notificar e tratamento de erros na tela.

Saídas reais: [execução](evidencias/execucao.txt) e [testes](evidencias/testes.txt).
As verificações de estrutura e preservação estão em
[estrutura](evidencias/estrutura.txt).
