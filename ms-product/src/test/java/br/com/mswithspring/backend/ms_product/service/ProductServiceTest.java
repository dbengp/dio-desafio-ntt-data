package br.com.mswithspring.backend.ms_product.service;

import br.com.mswithspring.backend.ms_product.exception.ProductNotFoundException;
import br.com.mswithspring.backend.ms_product.model.dto.ProductDto;
import br.com.mswithspring.backend.ms_product.model.entity.Product;
import br.com.mswithspring.backend.ms_product.repository.ProductRepository;
import br.com.mswithspring.backend.ms_product.service.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private ProductDto productDto1;
    private Product product2;
    private ProductDto productDto2;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Smartphone X", "Smartphone de última geração.", new BigDecimal("1299.99"));
        productDto1 = new ProductDto("Smartphone X", "Smartphone de última geração.", new BigDecimal("1299.99"));
        product2 = new Product(2L, "Notebook Gamer", "Notebook potente para jogos.", new BigDecimal("4500.00"));
        productDto2 = new ProductDto("Notebook Gamer", "Notebook potente para jogos.", new BigDecimal("4500.00"));
    }

    @Test
    @DisplayName("Deve retornar todos os produtos DTOs")
    void shouldReturnAllProductDtos() {

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
        when(productMapper.toDto(product1)).thenReturn(Optional.of(productDto1));
        when(productMapper.toDto(product2)).thenReturn(Optional.of(productDto2));

        List<ProductDto> result = productService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(productDto1.name(), result.get(0).name());
        assertEquals(productDto2.name(), result.get(1).name());

        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(1)).toDto(product1);
        verify(productMapper, times(1)).toDto(product2);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver produtos")
    void shouldReturnEmptyListWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductDto> result = productService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository, times(1)).findAll();

        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Deve retornar ProductDto pelo ID")
    void shouldReturnProductDtoById() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product1));
        when(productMapper.toDto(product1)).thenReturn(Optional.of(productDto1));

        ProductDto result = productService.findById(productId);

        assertNotNull(result);
        assertEquals(productDto1.name(), result.name());

        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).toDto(product1);
    }

    @Test
    @DisplayName("Deve lançar ProductNotFoundException quando produto não for encontrado pelo ID")
    void shouldThrowProductNotFoundExceptionWhenProductNotFoundById() {
        Long nonExistentId = 99L;

        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ProductNotFoundException thrown = assertThrows(ProductNotFoundException.class, () -> {
            productService.findById(nonExistentId);
        });

        assertEquals("Produto com ID " + nonExistentId + " não encontrado.", thrown.getMessage());

        verify(productRepository, times(1)).findById(nonExistentId);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando o mapeamento de entidade para DTO falhar em findById")
    void shouldThrowIllegalStateExceptionWhenMapperFailsInFindById() {
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.of(product1));

        when(productMapper.toDto(product1)).thenReturn(Optional.empty());

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            productService.findById(productId);
        });

        assertEquals("Erro ao mapear produto para DTO.", thrown.getMessage());

        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).toDto(product1);
    }

    @Test
    @DisplayName("Deve salvar e retornar um novo ProductDto")
    void shouldSaveAndReturnNewProductDto() {
        ProductDto newProductDto = new ProductDto("Novo Produto", "Descrição do novo produto.", new BigDecimal("100.00"));
        Product productToSave = new Product(null, "Novo Produto", "Descrição do novo produto.", new BigDecimal("100.00"));
        Product savedProduct = new Product(3L, "Novo Produto", "Descrição do novo produto.", new BigDecimal("100.00"));
        ProductDto savedProductDto = new ProductDto("Novo Produto", "Descrição do novo produto.", new BigDecimal("100.00"));

        when(productMapper.toEntity(newProductDto)).thenReturn(Optional.of(productToSave));
        when(productRepository.save(productToSave)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(Optional.of(savedProductDto));

        ProductDto result = productService.save(newProductDto);

        assertNotNull(result);
        assertEquals(savedProductDto.name(), result.name());
        assertEquals(savedProductDto.price(), result.price());

        verify(productMapper, times(1)).toEntity(newProductDto);
        verify(productRepository, times(1)).save(productToSave);
        verify(productMapper, times(1)).toDto(savedProduct);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ProductDto for nulo em save")
    void shouldThrowIllegalArgumentExceptionWhenProductDtoIsNullInSave() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.save(null);
        });

        assertEquals("DTO do produto não pode ser nulo para salvar.", thrown.getMessage());

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome do produto for nulo em save")
    void shouldThrowIllegalArgumentExceptionWhenProductNameIsNullInSave() {
        ProductDto invalidProductDto = new ProductDto(null, "Descrição.", new BigDecimal("10.00"));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.save(invalidProductDto);
        });

        assertEquals("Produto deve ter um nome.", thrown.getMessage());

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome do produto for vazio em save")
    void shouldThrowIllegalArgumentExceptionWhenProductNameIsBlankInSave() {
        ProductDto invalidProductDto = new ProductDto("", "Descrição.", new BigDecimal("10.00"));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.save(invalidProductDto);
        });

        assertEquals("Produto deve ter um nome.", thrown.getMessage());

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando o mapeamento de DTO para entidade falhar em save")
    void shouldThrowIllegalStateExceptionWhenMapperFailsToEntityInSave() {
        ProductDto newProductDto = new ProductDto("Produto Teste", "Descrição.", new BigDecimal("10.00"));

        when(productMapper.toEntity(newProductDto)).thenReturn(Optional.empty());

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            productService.save(newProductDto);
        });

        assertEquals("Erro ao converter DTO para entidade Product.", thrown.getMessage());

        verify(productMapper, times(1)).toEntity(newProductDto);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando o mapeamento de entidade salva para DTO falhar em save")
    void shouldThrowIllegalStateExceptionWhenMapperFailsToDtoAfterSave() {
        ProductDto newProductDto = new ProductDto("Produto Teste", "Descrição.", new BigDecimal("10.00"));
        Product productToSave = new Product(null, "Produto Teste", "Descrição.", new BigDecimal("10.00"));
        Product savedProduct = new Product(3L, "Produto Teste", "Descrição.", new BigDecimal("10.00"));

        when(productMapper.toEntity(newProductDto)).thenReturn(Optional.of(productToSave));
        when(productRepository.save(productToSave)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(Optional.empty());

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            productService.save(newProductDto);
        });

        assertEquals("Erro ao mapear produto salvo para DTO.", thrown.getMessage());

        verify(productMapper, times(1)).toEntity(newProductDto);
        verify(productRepository, times(1)).save(productToSave);
        verify(productMapper, times(1)).toDto(savedProduct);
    }

    @Test
    @DisplayName("Deve atualizar e retornar um ProductDto existente")
    void shouldUpdateAndReturnExistingProductDto() {
        Long productId = 1L;
        ProductDto updatedProductDto = new ProductDto("Smartphone X Atualizado", "Desc. atualizada.", new BigDecimal("1300.00"));
        Product existingProduct = new Product(productId, "Smartphone X", "Desc. original.", new BigDecimal("1299.99"));
        Product productAfterUpdate = new Product(productId, "Smartphone X Atualizado", "Desc. atualizada.", new BigDecimal("1300.00"));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productMapper.updateEntityFromDto(existingProduct, updatedProductDto)).thenReturn(productAfterUpdate);
        when(productRepository.save(productAfterUpdate)).thenReturn(productAfterUpdate);
        when(productMapper.toDto(productAfterUpdate)).thenReturn(Optional.of(updatedProductDto));

        ProductDto result = productService.update(productId, updatedProductDto);

        assertNotNull(result);
        assertEquals(updatedProductDto.name(), result.name());
        assertEquals(updatedProductDto.price(), result.price());

        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateEntityFromDto(existingProduct, updatedProductDto);
        verify(productRepository, times(1)).save(productAfterUpdate);
        verify(productMapper, times(1)).toDto(productAfterUpdate);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ProductDto for nulo em update")
    void shouldThrowIllegalArgumentExceptionWhenProductDtoIsNullInUpdate() {
        Long productId = 1L;

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.update(productId, null);
        });

        assertEquals("DTO do produto não pode ser nulo para atualização.", thrown.getMessage());

        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Deve lançar ProductNotFoundException quando produto não for encontrado em update")
    void shouldThrowProductNotFoundExceptionWhenProductNotFoundInUpdate() {
        Long nonExistentId = 99L;
        ProductDto updateDto = new ProductDto("Nome", "Descrição", new BigDecimal("10.00"));

        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ProductNotFoundException thrown = assertThrows(ProductNotFoundException.class, () -> {
            productService.update(nonExistentId, updateDto);
        });

        assertEquals("Produto com ID " + nonExistentId + " não encontrado.", thrown.getMessage());

        verify(productRepository, times(1)).findById(nonExistentId);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando o mapeamento de entidade atualizada para DTO falhar em update")
    void shouldThrowIllegalStateExceptionWhenMapperFailsToDtoAfterUpdate() {
        Long productId = 1L;
        ProductDto updatedProductDto = new ProductDto("Smartphone X Atualizado", "Desc. atualizada.", new BigDecimal("1300.00"));
        Product existingProduct = new Product(productId, "Smartphone X", "Desc. original.", new BigDecimal("1299.99"));
        Product productAfterUpdate = new Product(productId, "Smartphone X Atualizado", "Desc. atualizada.", new BigDecimal("1300.00"));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productMapper.updateEntityFromDto(existingProduct, updatedProductDto)).thenReturn(productAfterUpdate);
        when(productRepository.save(productAfterUpdate)).thenReturn(productAfterUpdate);
        when(productMapper.toDto(productAfterUpdate)).thenReturn(Optional.empty());

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            productService.update(productId, updatedProductDto);
        });

        assertEquals("Erro ao mapear produto atualizado para DTO.", thrown.getMessage());

        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateEntityFromDto(existingProduct, updatedProductDto);
        verify(productRepository, times(1)).save(productAfterUpdate);
        verify(productMapper, times(1)).toDto(productAfterUpdate);
    }


    @Test
    @DisplayName("Deve deletar um produto pelo ID com sucesso")
    void shouldDeleteProductByIdSuccessfully() {
        Long productId = 1L;

        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        assertDoesNotThrow(() -> productService.deleteById(productId));

        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("Deve lançar ProductNotFoundException ao tentar deletar produto inexistente")
    void shouldThrowProductNotFoundExceptionWhenDeletingNonExistentProduct() {
        Long nonExistentId = 99L;

        when(productRepository.existsById(nonExistentId)).thenReturn(false);

        ProductNotFoundException thrown = assertThrows(ProductNotFoundException.class, () -> {
            productService.deleteById(nonExistentId);
        });

        assertEquals("Produto com ID " + nonExistentId + " não encontrado.", thrown.getMessage());

        verify(productRepository, times(1)).existsById(nonExistentId);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
