package br.com.mswithspring.backend.ms_product.controller;

import br.com.mswithspring.backend.ms_product.exception.ProductNotFoundException;
import br.com.mswithspring.backend.ms_product.model.dto.ProductDto;
import br.com.mswithspring.backend.ms_product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;
//import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
@DisplayName("Testes para ProductController")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDto productDto1;
    private ProductDto productDto2;

    @BeforeEach
    void setUp() {
        // mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productDto1 = new ProductDto("Smartphone X", "Smartphone de última geração.", new BigDecimal("1299.99"));
        productDto2 = new ProductDto("Notebook Gamer", "Notebook potente para jogos.", new BigDecimal("4500.00"));
    }

    @Test
    @DisplayName("Deve retornar todos os produtos com status 200 OK")
    void shouldReturnAllProducts() throws Exception {
        List<ProductDto> products = Arrays.asList(productDto1, productDto2);

        given(productService.findAll()).willReturn(products);

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Smartphone X"))
                .andExpect(jsonPath("$[1].name").value("Notebook Gamer"));
    }

    @Test
    @DisplayName("Deve retornar um produto pelo ID com status 200 OK")
    void shouldReturnProductById() throws Exception {
        Long productId = 1L;

        given(productService.findById(anyLong())).willReturn(productDto1);

        mockMvc.perform(get("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smartphone X"));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando o produto não for encontrado pelo ID")
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        Long nonExistentId = 99L;

        given(productService.findById(anyLong())).willThrow(new ProductNotFoundException(nonExistentId));

        mockMvc.perform(get("/products/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Produto com ID " + nonExistentId + " não encontrado."));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o ID for negativo")
    void shouldReturnBadRequestWhenIdIsNegative() throws Exception {
        Long negativeId = -1L;

        mockMvc.perform(get("/products/{id}", negativeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Um ou mais parâmetros de entrada contêm erros de validação."))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    @DisplayName("Deve criar um novo produto com status 201 Created")
    void shouldCreateNewProduct() throws Exception {
        ProductDto newProductDto = new ProductDto("Novo Item", "Descrição do novo item.", new BigDecimal("50.00"));

        given(productService.save(any(ProductDto.class))).willReturn(newProductDto);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProductDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo Item"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar produto com nome vazio")
    void shouldReturnBadRequestWhenCreatingProductWithEmptyName() throws Exception {
        ProductDto invalidProductDto = new ProductDto("", "Descrição inválida.", new BigDecimal("10.00"));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Um ou mais campos contêm erros de validação."))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar produto com preço nulo")
    void shouldReturnBadRequestWhenCreatingProductWithNullPrice() throws Exception {
        ProductDto invalidProductDto = new ProductDto("Produto Sem Preço", "Descrição.", null);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Um ou mais campos contêm erros de validação."))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar criar produto com preço negativo")
    void shouldReturnBadRequestWhenCreatingProductWithNegativePrice() throws Exception {
        ProductDto invalidProductDto = new ProductDto("Produto Preço Negativo", "Descrição.", new BigDecimal("-1.00"));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Um ou mais campos contêm erros de validação."))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    @DisplayName("Deve atualizar um produto existente com status 200 OK")
    void shouldUpdateExistingProduct() throws Exception {
        Long productId = 1L;
        ProductDto updatedProductDto = new ProductDto("Produto Atualizado", "Descrição atualizada.", new BigDecimal("150.00"));

        given(productService.update(anyLong(), any(ProductDto.class))).willReturn(updatedProductDto);

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Produto Atualizado"));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar atualizar produto inexistente")
    void shouldReturnNotFoundWhenUpdatingNonExistentProduct() throws Exception {
        Long nonExistentId = 99L;
        ProductDto updateDto = new ProductDto("Produto Inexistente", "Descrição.", new BigDecimal("100.00"));

        given(productService.update(anyLong(), any(ProductDto.class)))
                .willThrow(new ProductNotFoundException(nonExistentId));

        mockMvc.perform(put("/products/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Deve deletar um produto pelo ID com status 204 No Content")
    void shouldDeleteProductById() throws Exception {
        Long productId = 1L;

        doNothing().when(productService).deleteById(anyLong());

        mockMvc.perform(delete("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar deletar produto inexistente")
    void shouldReturnNotFoundWhenDeletingNonExistentProduct() throws Exception {
        Long nonExistentId = 99L;

        doThrow(new ProductNotFoundException(nonExistentId))
                .when(productService).deleteById(anyLong());

        mockMvc.perform(delete("/products/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
