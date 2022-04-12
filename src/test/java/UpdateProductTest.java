import com.github.javafaker.Faker;
import com.google.gson.Gson;
import db.model.Products;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UpdateProductTest {
    static ProductService productService;
    static db.dao.ProductsMapper productsMapper;
    static SqlSession session;
    static db.model.ProductsExample example;
    Product product = null;
    Faker faker = new Faker();
    int id;
    String titleOld;
    int priceOld;

    @BeforeAll
    static void beforeAll() {
        productService = RetrofitUtils.getRetrofit()
                .create(ProductService.class);
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

    @BeforeEach
    void setUp() throws IOException {
        product = new Product()
                .withTitle(faker.food().ingredient())
                .withCategoryTitle("Food")
                .withPrice((int) (Math.random() * 10000));
        Response<Product> response = productService.createProduct(product)
                .execute();
        id = response.body().getId();
        titleOld = response.body().getTitle();
        priceOld = response.body().getPrice();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
    }

    @Test
    void updateProductPositiveTest() throws IOException {
        example.createCriteria().andCategory_idEqualTo((long)id);
        List<Products> listProducts = productsMapper.selectByExample(example);
        db.model.Products products = listProducts.get(0);
        assertThat(products.getId(), equalTo(id));
        assertThat(products.getTitle(), equalTo(titleOld));
        assertThat(products.getPrice(), equalTo(priceOld));

        String titleNew = faker.food().ingredient();
        int priceNew = priceOld + 1;
        product = new Product()
                .withId(id)
                .withTitle(titleNew)
                .withCategoryTitle("Food")
                .withPrice(priceNew);
        Response<Product> response = productService.modifyProduct(product)
                .execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        listProducts = productsMapper.selectByExample(example);
        products = listProducts.get(0);
        assertThat(products.getId(), equalTo(id));
        assertThat(products.getTitle(), equalTo(titleNew));
        assertThat(products.getPrice(), equalTo(priceNew));
    }

    @Test
    void createProductNegativeTest() throws IOException {
        product = new Product()
                .withId(999)
                .withTitle(faker.food().ingredient())
                .withCategoryTitle("Food")
                .withPrice((int) (Math.random() * 10000));
        Response<Product> response = productService.modifyProduct(product)
                .execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(false));
        ErrorResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), ErrorResponse.class);
        assertThat(errorResponse.getStatus(), equalTo(400));
        assertThat(errorResponse.getMessage(), equalTo("Product with id: 999 doesn't exist"));
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        Response<ResponseBody> response = productService.deleteProduct(id).execute();
    }

    @AfterAll
    void afterAll() {
        session.close();
    }
}

