package aulara;

import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class AppTest {
    String userToken;
    String userID;
    String productID;

    Faker faker = new Faker();

    Usuarios usuarios = new Usuarios();
    Produtos produtos = new Produtos();

    String userName = faker.name().firstName();
    String userEmail = userName + "@qa.com.br";
    long value = faker.number().randomNumber();
    String productName = "Computador" + value ;

    @BeforeClass
    public static void preCondition(){
        baseURI  = "http://localhost";
        port = 3000;
    }
    @Test
    public void getUsuarios(){
        when()
                .get("/usuarios")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
    @Test
    public void validarEstoque(){
        //Cadastrar usuário
        String userID =
        given()
                .body("{\n" +
                        "  \"nome\": \"Natália Belo\",\n" +
                        "  \"email\": \"nbteste@qa.com.br\",\n" +
                        "  \"password\": \"teste\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("usuarios")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

        //Autenticar usuario
        String userToken = given()
                .body("{\n" +
                        "  \"email\": \"nbteste@qa.com.br\",\n" +
                        "  \"password\": \"teste\"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/login")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Login realizado com sucesso"))
                .extract().path("authorization");

        //Cadastrar produto
        String productID = given()
                .header("authorization", userToken)
                .body("{\n" +
                        "  \"nome\": \"Logitech Vertical Teste\",\n" +
                        "  \"preco\": 470,\n" +
                        "  \"descricao\": \"Mouse\",\n" +
                        "  \"quantidade\": 500\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/produtos")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

        //Cadastrar carrinho
        given()
                .header("authorization", userToken)
                .body("{\n" +
                        "  \"produtos\": [\n" +
                        "    {\n" +
                        "      \"idProduto\": \"" + productID + "\",\n" +
                        "      \"quantidade\": 1\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/carrinhos")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"));

        //Validar estoque diminuído
        given()
                .pathParam("_id", productID)
        .when()
                .get("/produtos/{_id}")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("quantidade", is(499));

        //Cancelar compra
        given()
                .header("authorization", userToken)
        .when()
                .delete("/carrinhos/cancelar-compra")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso. Estoque dos produtos reabastecido"));

        //Validar estoque restabelecido
        given()
                .pathParam("_id", productID)
        .when()
                .get("/produtos/{_id}")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("quantidade", is(500));

        //Excluir produto
        given()
                .pathParam("_id", productID)
                .header("authorization", userToken)
        .when()
                .delete("/produtos/{_id}")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));

        //Excluir usuário
        given()
                .pathParam("_id", userID)
                .header("authorization", userToken)
        .when()
                .delete("/usuarios/{_id}")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));

    }


    @Test
    public void exercicio2(){
        userID = usuarios.cadastrarUsuario(userName, userEmail);
        userToken = usuarios.autenticarUsuario(userEmail);
        productID = produtos.cadastrarProduto(userToken, productName);
        cadastrarCarrinho();
        excluirUsuarioCarrinhoAssociado();
        excluirCarrinhoCancelando();
        excluirProduto();
        excluirUsuario();
    }



    public void cadastrarCarrinho(){
        given()
                .header("authorization", userToken)
                .body("{\n" +
                        "  \"produtos\": [\n" +
                        "    {\n" +
                        "      \"idProduto\": \""+productID+"\",\n" +
                        "      \"quantidade\": 1\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/carrinhos")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"));

    }

    public void excluirUsuarioCarrinhoAssociado(){
        given()
                .pathParam("_id", userID)
                .header("authorization",userToken)
        .when()
                .delete("/usuarios/{_id}")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message",is("Não é permitido excluir usuário com carrinho cadastrado"));

    }

    public void excluirCarrinhoCancelando(){
        given()
                .header("authorization", userToken)
        .when()
                .delete("/carrinhos/cancelar-compra")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso. Estoque dos produtos reabastecido"));
    }

    public void excluirProduto(){
        given()
                .pathParam("_id", productID)
                .header("authorization",userToken)
        .when()
                .delete("/produtos/{_id}")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message",is("Registro excluído com sucesso"));
    }

    public void excluirUsuario(){
        given()
                .pathParam("_id", userID)
                .header("authorization",userToken)
        .when()
                .delete("/usuarios/{_id}")
        .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message",is("Registro excluído com sucesso"));
    }



}
