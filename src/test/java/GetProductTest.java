import com.google.gson.Gson;
import db.model.Products;
import lombok.SneakyThrows;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GetProductTest {

    static ProductService productService;
    static db.dao.ProductsMapper productsMapper;
    static SqlSession session;
    static db.model.ProductsExample example;

    @BeforeAll
    static void beforeAll() {
        productService = RetrofitUtils.getRetrofit().create(ProductService.class);
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new
                SqlSessionFactoryBuilder().build(inputStream);
        session = sqlSessionFactory.openSession();
        productsMapper = session.getMapper(db.dao.ProductsMapper.class);
        example = new db.model.ProductsExample();
    }

    @SneakyThrows
    @Test
    void getProductByIdPositiveTest() {
        int id = 1;

        example.createCriteria().andCategory_idEqualTo((long)id);
        List<Products> listProducts = productsMapper.selectByExample(example);
        db.model.Products products = listProducts.get(0);

        Response<Product> response = productService.getProductById(id).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        assertThat(response.body().getId(), equalTo(Integer.valueOf(products.getId().intValue())));
        assertThat(response.body().getTitle(), equalTo(products.getTitle()));
        assertThat(response.body().getPrice(), equalTo(products.getPrice()));
    }

    @SneakyThrows
    @Test
    void getProductByIdNegativeTest() {
        Response<Product> response = productService.getProductById(11).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
        ErrorResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), ErrorResponse.class);
        assertThat(errorResponse.getStatus(), equalTo(404));
        assertThat(errorResponse.getMessage(), equalTo("Unable to find product with id: 11"));
    }
    @AfterAll
    void afterAll() {
        session.close();
    }
}

