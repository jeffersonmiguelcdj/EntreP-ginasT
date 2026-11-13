# Receita para o Render compilar e rodar o servidor Java (ServidorWeb).
# Fica na raiz do repositório, ao lado das pastas entrepaginas-java e entrepaginas-web.

# Etapa 1: compila o projeto com o Maven (Java 21, igual ao pom.xml)
FROM maven:3.9-eclipse-temurin-21 AS compilacao
WORKDIR /app/entrepaginas-java
COPY entrepaginas-java/pom.xml .
RUN mvn -q dependency:copy-dependencies -DoutputDirectory=target/dependency
COPY entrepaginas-java/src ./src
RUN mvn -q compile

# Etapa 2: imagem final, só com o Java e o que foi compilado
FROM eclipse-temurin:21-jre
WORKDIR /app/entrepaginas-java
COPY --from=compilacao /app/entrepaginas-java/target/classes ./classes
COPY --from=compilacao /app/entrepaginas-java/target/dependency ./dependency
# O ServidorWeb também entrega o site a partir de ../entrepaginas-web
COPY entrepaginas-web /app/entrepaginas-web
EXPOSE 8080
# A senha do banco NÃO fica aqui: o Render passa DB_URL, DB_USER e DB_PASSWORD como variáveis de ambiente
CMD ["java", "-cp", "classes:dependency/*", "servidor.ServidorWeb"]
