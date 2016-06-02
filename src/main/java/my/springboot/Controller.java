package my.springboot;

import org.apache.tomcat.util.digester.ArrayStack;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rx.Observable;
import rx.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by I311862 on 2016/5/27.
 */
@RestController
public class Controller {

    @RequestMapping("/")
    public String hello() {
        return new CommandHelloWorld("hystrix").execute();
    }

    @RequestMapping("/google")
    public String getGoogle(){
        return new CommandHttpCall("http://www.google.com").execute();
    }

    @RequestMapping("/product")
    public String getProduct() throws InterruptedException {
        Thread.sleep(1000);
        return new CommandHelloWorld("this is content for product").execute();
    }

    @RequestMapping("/order")
    public String getOrder() throws InterruptedException {
        Thread.sleep(1000);
        return new CommandHelloWorld("this is content for order").execute();
    }
    @RequestMapping("/cart")
    public String getCart() throws InterruptedException {
        Thread.sleep(1000);
        return new CommandHelloWorld("this is content for cart").execute();
    }

    @RequestMapping("/observe")
    public String getObserve() throws InterruptedException {
        Observable<String> productCall = new CommandHttpCall("http://localhost:8091/product").observe();
        Observable<String> orderCall = new CommandHttpCall("http://localhost:8091/order").observe();
        Observable<String> cartCall = new CommandHttpCall("http://localhost:8091/cart").observe();

        List<Observable<String>> result = new ArrayList<>();
        result.add(productCall);
        result.add(orderCall);
        Observable.merge(result).subscribe(new Observer<String>() {

            @Override
            public void onCompleted() {
                System.out.println("product&order call complete");
                cartCall.subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("cart call complete");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String v) {
                        System.out.println("onNext: " + v);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(String v) {
                System.out.println("onNext: " + v);
            }

        });

        return new CommandHelloWorld("this is content for observe").execute();
    }

    @RequestMapping("/future")
    public String getFuture() throws InterruptedException {
        Future<String> productSyncCall = new CommandHttpCall("http://localhost:8091/product").queue();

        try {
            String product = productSyncCall.get();
            System.out.println("sync get product" + product);
            Future<String> orderSyncCall = new CommandHttpCall("http://localhost:8091/order").queue();
            Future<String> cartSyncCall = new CommandHttpCall("http://localhost:8091/cart").queue();
            System.out.println("sync get order" + orderSyncCall.get());
            System.out.println("sync get cart" + cartSyncCall.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return new CommandHelloWorld("this is content for future").execute();
    }
}