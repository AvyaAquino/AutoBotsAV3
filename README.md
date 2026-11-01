# Autobots

Microsserviço de gestão para lojas de manutenção veicular e venda de autopeças, feito com Spring Boot.
Implementa o diagrama de classes da ATVIII (empresa, usuários, veículos, mercadorias, serviços e vendas),
com CRUD completo para todas as entidades e HATEOAS.

## Como executar

Pré-requisitos: Java 17+, Maven e MySQL rodando em `localhost:3306` (usuário `root`, senha `root`).
Se não tiver o Maven instalado, use o wrapper do projeto: `.\mvnw.cmd` no Windows ou `./mvnw` no Linux/macOS.

```bash
cd automanager
mvn clean spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

O banco `base` é criado automaticamente. A cada inicialização as tabelas são recriadas e preenchidas com dados
de exemplo: uma empresa, um cliente (id 1), um funcionário (id 2), um fornecedor (id 3), um veículo e duas vendas.

## Endpoints

Todas as entidades seguem o mesmo padrão: listar, buscar por id, cadastrar, atualizar e excluir.
Atualizar e excluir recebem no corpo um JSON com o `id`.

| Entidade   | Listar                         | Buscar                 | Cadastrar                                                       |
|------------|--------------------------------|------------------------|-----------------------------------------------------------------|
| Empresa    | GET `/empresa/empresas`        | GET `/empresa/{id}`    | POST `/empresa/cadastro`                                        |
| Usuário    | GET `/usuario/usuarios`        | GET `/usuario/{id}`    | POST `/usuario/cadastro/{empresaId}`                            |
| Veículo    | GET `/veiculo/veiculos`        | GET `/veiculo/{id}`    | POST `/veiculo/cadastro/{usuarioId}`                            |
| Mercadoria | GET `/mercadoria/mercadorias`  | GET `/mercadoria/{id}` | POST `/mercadoria/cadastro/{empresaId}` (opcional `?fornecedorId=`) |
| Serviço    | GET `/servico/servicos`        | GET `/servico/{id}`    | POST `/servico/cadastro/{empresaId}`                            |
| Venda      | GET `/venda/vendas`            | GET `/venda/{id}`      | POST `/venda/cadastro/{empresaId}`                              |
| Telefone   | GET `/telefone/telefones`      | GET `/telefone/{id}`   | POST `/telefone/cadastro/usuario/{id}` ou `/telefone/cadastro/empresa/{id}` |
| E-mail     | GET `/email/emails`            | GET `/email/{id}`      | POST `/email/cadastro/{usuarioId}`                              |
| Documento  | GET `/documento/documentos`    | GET `/documento/{id}`  | POST `/documento/cadastro/{usuarioId}`                          |
| Endereço   | GET `/endereco/enderecos`      | GET `/endereco/{id}`   | POST `/endereco/cadastro/usuario/{id}` ou `/endereco/cadastro/empresa/{id}` |
| Credencial | GET `/credencial/credenciais`  | GET `/credencial/{id}` | POST `/credencial/cadastro/{usuarioId}`                         |

Atualizar: PUT `/{entidade}/atualizar` · Excluir: DELETE `/{entidade}/excluir`

Códigos de resposta: `200` ok · `201` criado · `400` dados inválidos · `404` não encontrado · `409` conflito
(registro duplicado ou ainda ligado a outros, como uma mercadoria que já foi vendida).

## Regras de negócio

- Todo usuário é cadastrado já associado a uma empresa e precisa ter ao menos um perfil: `CLIENTE`, `FUNCIONARIO` ou `FORNECEDOR`.
- Veículos só podem ter como proprietário um usuário com perfil `CLIENTE`.
- Na venda, cliente, funcionário, veículo, mercadorias e serviços são informados pelo `id`. O cliente precisa ser
  cliente, o funcionário precisa ser funcionário e o veículo precisa pertencer ao cliente.
- Mercadorias, serviços, veículos e usuários que aparecem em vendas não podem ser excluídos (`409`).
- Credenciais podem ser de dois tipos, indicados pelo campo `"tipo"`: `usuarioSenha` ou `codigoBarra`.
  A senha é recebida, mas nunca devolvida nas respostas.

## HATEOAS

As respostas trazem links para o próprio recurso (`self`) e para a coleção (por exemplo, `empresas`),
atendendo ao nível 3 do modelo de maturidade de Richardson.

## Testes

A pasta `automanager` contém a coleção do Postman `automanager-atviii.postman_collection.json`, com todas as rotas
separadas por entidade. Os ids criados nos cadastros são guardados em variáveis e usados nas requisições de
atualizar e excluir. Para rodar tudo: reinicie a aplicação e use **Run collection**.