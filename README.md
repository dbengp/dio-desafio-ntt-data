# **dio-desafio-ntt-data**

## **Arquitetura de Microsserviços com Spring Boot**

Este repositório contém uma arquitetura de microsserviços desenvolvida com Spring Boot, utilizando Spring Cloud para comunicação e descoberta de serviços, e Keycloak como Identity Provider para autenticação e autorização.

## **Visão Geral da Arquitetura**

A arquitetura é composta por um conjunto de serviços independentes que se comunicam para formar um sistema coeso. O ponto de entrada para todas as requisições externas é um API Gateway, que também atua como camada de segurança centralizada. A descoberta de serviços é gerenciada por um Eureka Server, e a autenticação é delegada ao Keycloak.

### **Componentes Principais:**

1. **Eureka (Discovery Service)**  
2. **Keycloak (Identity Provider \- IdP)**  
3. **Gateway (API Gateway & Camada de Segurança)**  
4. **MS-Product (Microserviço de Produtos)**  
5. **MS-Order (Microserviço de Pedidos)**

### **Detalhes dos Componentes**

#### **1\. Eureka (Discovery Service)**

* **Função:** Servidor de registro e descoberta de serviços. Permite que os microsserviços se registrem e localizem uns aos outros dinamicamente.  
* **Tecnologias:** Spring Boot 3.5.3, Spring Cloud Netflix Eureka Server 2025.0.0, Java 21\.  
* **Execução:** Aplicação Spring Boot rodando diretamente no ambiente Windows, a partir do arquivo .jar (eureka-0.0.1-SNAPSHOT.jar). Esta abordagem foi escolhida pela simplicidade e economia de recursos em ambiente de desenvolvimento.  
* **Porta:** 8761  
* **Dependências Chave (build.gradle.kts):**  
  * org.springframework.cloud:spring-cloud-starter-netflix-eureka-server  
* **Configuração Chave (application.yml):**  
  * spring.application.name: eureka  
  * eureka.client.register-with-eureka: false  
  * eureka.client.fetch-registry: false

#### **2\. Keycloak (Identity Provider \- IdP)**

* **Função:** Atua como o servidor de autenticação e autorização central. Gerencia usuários, reinos (realms), clientes e emite JSON Web Tokens (JWTs) após autenticação bem-sucedida.  
* **Tecnologias:** Keycloak 24.0.4, PostgreSQL 13\.  
* **Execução:** Rodando em containers Docker (keycloak e seu banco de dados postgres\_to\_keycloak) dentro do ambiente WSL2 no Windows.  
* **Porta:** 8082 (mapeada da porta 8080 interna do container Keycloak para a porta 8082 do host WSL2/Windows, evitando conflito com o Gateway).  
* **Dependências Chave (Docker Compose):** Não se aplica diretamente a dependências de código, mas sim a serviços Docker.  
* **Configurações Chave (docker-compose.yml):**  
  * keycloak service:  
    * image: quay.io/keycloak/keycloak:24.0.4  
    * ports: "8082:8080"  
    * environment:  
      * KC\_DB\_USERNAME: dev  
      * KC\_DB\_PASSWORD: p0stgreS  
      * KC\_DB\_URL: jdbc:postgresql://postgres\_to\_keycloak/keycloak  
      * KEYCLOAK\_ADMIN: admin  
      * KEYCLOAK\_ADMIN\_PASSWORD: admin  
      * KC\_HOSTNAME: localhost  
      * KC\_HOSTNAME\_PORT: 8080  
      * KC\_HTTP\_RELATIVE\_PATH: /auth  
  * postgres service (para Keycloak):  
    * image: postgres:13  
    * environment:  
      * POSTGRES\_DB: keycloak  
      * POSTGRES\_USER: dev  
      * POSTGRES\_PASSWORD: p0stgreS  
* **Configuração no Console Keycloak:**  
  * **Realm:** Criado um realm dedicado (ex: ms-product-realm).  
  * **Client:** Criado um cliente (ex: gateway-client) com as seguintes configurações:  
    * Client authentication: Off  
    * Standard flow: Habilitado  
    * Direct access grants: Habilitado (útil para testes via curl).  
    * Root URL: http://localhost:8080  
    * Valid redirect URIs: http://localhost:8080/\*  
    * Web origins: http://localhost:8080  
  * **Usuários e Roles:** Usuários e roles (ex: USER, ADMIN) são criados e mapeados dentro do realm para fins de teste.

#### **3\. Gateway (API Gateway & Camada de Segurança)**

* **Função:** Ponto de entrada unificado para todas as requisições externas. Roteia requisições para os microsserviços apropriados e atua como a camada de segurança centralizada, validando JWTs emitidos pelo Keycloak.  
* **Tecnologias:** Spring Boot 3.5.3, Spring Cloud Gateway 2025.0.0, Spring Boot Security, Spring Security OAuth2 Resource Server, Spring Security OAuth2 JOSE, Spring Cloud Netflix Eureka Client, Java 21\.  
* **Execução:** Aplicação Spring Boot rodando diretamente no ambiente Windows.  
* **Porta:** 8080 (evita conflito com o Keycloak que está na 8082).  
* **Dependências Chave (build.gradle.kts):**  
  * org.springframework.cloud:spring-cloud-starter-gateway  
  * org.springframework.cloud:spring-cloud-starter-netflix-eureka-client  
  * org.springframework.boot:spring-boot-starter-security  
  * org.springframework.security:spring-security-oauth2-resource-server  
  * org.springframework.security:spring-security-oauth2-jose  
* **Configurações Chave (application.yml):**  
  * spring.application.name: gateway  
  * server.port: 8080  
  * eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/  
  * spring.cloud.gateway.routes: Define as rotas para os microsserviços (ex: /products/\*\* para lb://MS-PRODUCT).  
  * spring.security.oauth2.resourceserver.jwt.issuer-uri: Aponta para o Keycloak (ex: http://localhost:8082/auth/realms/ms-product-realm).  
* **Configuração de Segurança (SecurityConfig.java):**  
  * @EnableWebFluxSecurity: Habilita a segurança reativa para o Gateway.  
  * Desabilita CSRF para APIs stateless.  
  * authorizeExchange(exchanges \-\> exchanges.pathMatchers("/eureka/\*\*").permitAll().anyExchange().authenticated()): Permite acesso público ao Eureka e exige autenticação para todas as outras rotas.  
  * oauth2ResourceServer(oauth2 \-\> oauth2.jwt(jwt \-\> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))): Configura o Gateway como um Resource Server JWT, utilizando um conversor personalizado para extrair roles do JWT (realm\_access.roles) e mapeá-las para GrantedAuthority.  
  * GlobalFilter customGlobalFilter(): Um filtro global que extrai o ID do usuário (sub claim) e as roles do JWT, adicionando-os como cabeçalhos HTTP (X-User-ID, X-User-Roles) para os microsserviços downstream.  
* Importando a Collection Postman <https://github.com/dbengp/dio-desafio-ntt-data/blob/44dd47fbfa7876654e5c418f469c4f845c30d3b1/postman/dio-desafio-ntt-data.postman_collection.json>:  
  Para facilitar a validação das chamadas através do Gateway, foi gerada uma collection do Postman chamada dio-desafio-ntt-data.postman_collection.json. Siga os passos abaixo para importá-la:  
  1. **Abra o Postman.**  
  2. No menu superior esquerdo, clique em **File** (Arquivo) \> **Import** (Importar).  
  3. Na janela de importação, clique na aba **Upload Files** (Carregar Arquivos).  
  4. Selecione o arquivo dio-desafio-ntt-data.postman\_collection do seu sistema de arquivos.  
  5. Clique em **Import** (Importar).
  6. Altere o valor das variáveis do environment de acordo com as suas configurações do keycloak como password, username, client-id e outros gerado pelo keycloak

A collection será adicionada à sua lista de Collections no Postman, permitindo que você execute facilmente as requisições para o ms-product e ms-order através do Gateway. Certifique-se de configurar o ambiente conforme o guia de Collection Postman para que as variáveis (URLs, tokens) funcionem corretamente.

#### **4\. MS-Product (Microserviço de Produtos)**

* **Função:** Gerencia a lógica de negócio para operações CRUD (Criar, Ler, Atualizar, Deletar) de produtos.  
* **Tecnologias:** Spring Boot 3.5.3, Spring Data JPA, PostgreSQL Driver, Spring Cloud Netflix Eureka Client, Jakarta Validation, Java 21\.  
* **Execução:** Aplicação Spring Boot rodando diretamente no ambiente Windows.  
* **Porta:** 8081  
* **Dependências Chave (build.gradle.kts):**  
  plugins {  
      java  
      id("org.springframework.boot") version "3.5.3"  
      id("io.spring.dependency-management") version "1.1.7"  
      id("jacoco") // Plugin do JaCoCo para cobertura de código  
  }

  group \= "br.com.mswithspring.backend"  
  version \= "0.0.1-SNAPSHOT"

  java {  
      toolchain {  
          languageVersion \= JavaLanguageVersion.of(21)  
      }  
  }

  repositories {  
      mavenCentral()  
  }

  extra\["springCloudVersion"\] \= "2025.0.0"

  dependencies {  
      implementation("org.springframework.boot:spring-boot-starter-web")  
      implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")  
      implementation("org.springframework.boot:spring-boot-starter-validation")  
      implementation("org.springframework.boot:spring-boot-starter-data-jpa")  
      runtimeOnly("org.postgresql:postgresql")  
      testImplementation("org.springframework.boot:spring-boot-starter-test")  
      testRuntimeOnly("org.junit.platform:junit-platform-launcher")  
  }

  dependencyManagement {  
      imports {  
          mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")  
      }  
  }

  tasks.withType\<Test\> {  
      useJUnitPlatform()  
  }

  jacoco {  
      toolVersion \= "0.8.12" // Versão da ferramenta JaCoCo  
  }

  tasks.jacocoTestReport {  
      dependsOn(tasks.test) // Garante que os testes rodem antes do relatório  
      reports {  
          xml.required.set(true) // Gera relatório em XML  
          csv.required.set(false) // Não gera relatório em CSV  
          html.required.set(true) // Gera relatório em HTML (fácil de visualizar)  
      }  
  }

* **Configurações Chave (application.yml):**  
  * spring.application.name: ms-product  
  * server.port: 8081  
  * eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/  
  * spring.datasource.url: jdbc:postgresql://localhost:5432/ms\_product\_db  
  * spring.jpa.hibernate.ddl-auto: create (recria o esquema do DB a cada inicialização, ideal para desenvolvimento).  
  * spring.jpa.defer-datasource-initialization: true (garante que data.sql seja executado após o DDL do Hibernate).  
  * spring.sql.init.mode: always (garante que data.sql seja sempre executado para DBs externos).  
* **Persistência:** Utiliza um container Docker de PostgreSQL (db\_postgres) para armazenamento de dados. O container é acessível via localhost:5432 do Windows.  
* **Carga Inicial:** Possui um script data.sql em src/main/resources que popula a tabela tb\_products com 10 linhas na inicialização da aplicação.  
* **Modelo de Dados:**  
  * Product.java (Entidade JPA): Mapeia a tabela tb\_products, inclui id, name, description e o atributo price do tipo BigDecimal com precisão e escala definidas (@Column(precision \= 10, scale \= 2)).  
  * ProductDto.java (Record DTO): Usado para entrada e saída de dados na API. Inclui name, description e price. Contém validações (@NotBlank, @Size, @NotNull, @DecimalMin) aplicadas via @Valid no ProductController.  
* **Camada de Negócio:** ProductService.java (lógica de negócio) e ProductMapper.java (conversão entre DTO e Entidade, lidando com o campo price).  
* **Controlador (ProductController.java):** Expõe endpoints REST (/products) para operações CRUD. Utiliza @Valid para validação do corpo da requisição e @Validated com @Positive para validação de IDs em @PathVariable.  
* **Tratamento de Erros:** ProductNotFoundException.java (exceção personalizada) e GlobalExceptionHandler.java (@ControllerAdvice) para tratamento centralizado de exceções (incluindo validações de DTO e parâmetros, e erros de tipo de argumento), retornando respostas HTTP padronizadas.  
* **Testes Unitários e Cobertura de Código:**  
  * **Propósito:** Garantir a qualidade e o comportamento esperado das camadas de Controller e Service de forma isolada.  
  * **Frameworks e Ferramentas:**  
    * **JUnit 5:** Framework de testes.  
    * **Mockito:** Biblioteca para criação de mocks, permitindo simular dependências e isolar as unidades de código.  
    * **Spring Boot Test:** Fornece utilitários para testes de aplicações Spring Boot.  
    * **JaCoCo (Java Code Coverage):** Ferramenta para gerar relatórios de cobertura de código.  
  * **Estratégia de Teste:**  
    * **ProductControllerTest.java**: Utiliza @WebMvcTest para carregar apenas o contexto web do Spring, focando nos endpoints REST. Dependências como ProductService são mockadas usando @MockBean e as requisições são simuladas com MockMvc.  
    * **ProductServiceTest.java**: Utiliza @ExtendWith(MockitoExtension.class), @Mock para ProductRepository e ProductMapper, e @InjectMocks para ProductService. Isso permite testar a lógica de negócio do serviço sem interagir com o banco de dados real.  
  * **Execução dos Testes e Geração do Relatório de Cobertura:**  
    * Para executar todos os testes unitários e gerar o relatório de cobertura JaCoCo, navegue até a raiz do diretório ms-product no terminal e execute:  
      ./gradlew clean test jacocoTestReport

    * **Configuração de Teste (Opcional \- para limpar logs):** Para evitar mensagens de erro de conexão com o Eureka durante os testes unitários (que não dependem do Eureka), pode-se criar o arquivo ms-product/src/test/resources/application.yml com o seguinte conteúdo:  
      eureka:  
        client:  
          enabled: false \# Desabilita o cliente Eureka para o perfil de teste

  * **Visualização do Relatório de Cobertura:**  
    * O relatório HTML detalhado da cobertura de código é gerado em:  
      ms-product/build/reports/jacoco/test/html/index.html  
    * Este relatório mostra a porcentagem de linhas, branches e métodos cobertos pelos testes, auxiliando na identificação de áreas que precisam de mais cobertura.

#### **5\. MS-Order (Microserviço de Pedidos)**

* **Função:** Simula a criação de pedidos. Interage com o MS-Product para buscar informações sobre produtos disponíveis. **Não possui persistência própria.**  
* **Tecnologias:** Spring Boot 3.5.3, Spring Cloud Netflix Eureka Client, Spring Cloud OpenFeign, Jakarta Validation, Java 21\.  
* **Execução:** Aplicação Spring Boot rodando diretamente no ambiente Windows.  
* **Porta:** 8083 (evita conflitos com outros serviços).  
* **Dependências Chave (build.gradle.kts):**  
  * org.springframework.boot:spring-boot-starter-web  
  * org.springframework.cloud:spring-cloud-starter-netflix-eureka-client  
  * org.springframework.boot:spring-boot-starter-validation  
  * org.springframework.cloud:spring-cloud-starter-openfeign  
* **Configurações Chave (application.yml):**  
  * spring.application.name: ms-order  
  * server.port: 8083  
  * eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/  
  * feign.client.config.default.loggerLevel: basic (para logs básicos de requisições Feign).  
  * logging.level.br.com.mswithspring.backend.ms\_order.client.ProductClient: INFO (para logs específicos do cliente Feign).  
* **Modelo de Dados:** DTOs específicos para pedidos (OrderDto, OrderItemDto, OrderConfirmationDto) e uma cópia local do ProductDto para consumir dados do MS-Product.  
* **Camada de Negócio:** OrderService.java (lógica de simulação de pedidos, que interage com ProductClient).  
* **Controlador (OrderController.java):** Expõe um endpoint POST /orders/simulate para receber e validar pedidos simulados, delegando ao OrderService.  
* **Tratamento de Erros:** OrderCreationException.java (exceção personalizada) e GlobalExceptionHandler.java (@ControllerAdvice) para tratamento centralizado de exceções.

## **Ordem de Inicialização dos Serviços**

Para garantir que todas as dependências sejam atendidas, os serviços devem ser iniciados na seguinte ordem:  
Obs 1.: No meu caso optei for gerar "jars", mas se prefeir abra cada API em seu IDE e execute com SHIFT \+ F10 (no Intellij):  
Obs 2.: Tive que usar os containers direto no WSL2 pois o Docker Desktop não funcionou\!

1. **Bancos de Dados:**  
   * PostgreSQL para ms-product (docker compose up \-d).  
   * PostgreSQL para Keycloak e Keycloak (docker compose up \-d).  
2. **Eureka Server:** (java \-jar eureka-0.0.1-SNAPSHOT.jar)  
3. **MS-Product:** (java \-jar ms-product-0.0.1-SNAPSHOT.jar)  
4. **MS-Order:** (java \-jar ms-order-0.0.1-SNAPSHOT.jar)  
5. **Gateway:** (java \-jar gateway-0.0.1-SNAPSHOT.jar)

graph TD  
    subgraph Cliente/Usuário  
        C\[Cliente/Usuário\]  
    end

    subgraph Infraestrutura\_Docker\_WSL2  
        subgraph Keycloak  
            KC\[Keycloak:8082\]  
            KCDb(Keycloak DB)  
            KC \--- KCDb  
        end  
        subgraph MS\_Product\_DB  
            MSPDb(PostgreSQL:5432)  
        end  
    end

    subgraph APIs\_Spring\_Boot  
        GW\[Gateway:8080\]  
        E\[Eureka Server:8761\]  
        MSP\[MS-Product:8081\]  
        MSO\[MS-Order:8083\]  
    end

    C \--\> GW  
    GW \-- Autenticação JWT \--\> KC  
    GW \-- Descoberta de Serviço \--\> E  
    MSP \-- Registro/Descoberta \--\> E  
    MSO \-- Registro/Descoberta \--\> E  
    MSP \-- Persistência \--\> MSPDb  
    MSO \-- Busca de Produtos \--\> MSP

    style C fill:\#f9f,stroke:\#333,stroke-width:2px  
    style GW fill:\#bbf,stroke:\#333,stroke-width:2px  
    style KC fill:\#ccf,stroke:\#333,stroke-width:2px  
    style E fill:\#bfb,stroke:\#333,stroke-width:2px  
    style MSP fill:\#ffc,stroke:\#333,stroke-width:2px  
    style MSO fill:\#cff,stroke:\#333,stroke-width:2px  
    style MSPDb fill:\#fcc,stroke:\#333,stroke-width:2px  
    style KCDb fill:\#fcc,stroke:\#333,stroke-width:2px

    linkStyle 0 stroke:\#000,stroke-width:2px,color:black;  
    linkStyle 1 stroke:\#a00,stroke-width:2px,color:red;  
    linkStyle 2 stroke:\#0a0,stroke-width:2px,color:green;  
    linkStyle 3 stroke:\#0a0,stroke-width:2px,color:green;  
    linkStyle 4 stroke:\#0a0,stroke-width:2px,color:green;  
    linkStyle 5 stroke:\#00f,stroke-width:2px,color:blue;  
    linkStyle 6 stroke:\#00f,stroke-width:2px,color:blue;  

#### Esse README foi criado com auxílio massivo do Gemini 2.5 flash! Ficou muito bom por sinal.
