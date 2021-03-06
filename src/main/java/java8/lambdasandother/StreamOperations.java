package java8.lambdasandother;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

// stream in Java 8 doesn't have the error channel and complete channel. It has the data channel. It's hard to deal with exception handling
public class StreamOperations {

    // functional programming:
    // immutability
    // higher-order functions (pass the function to another function, we could create function that return functions from the functions)
    // function pipeline
    // immutability

    // we are going down stream. If something goes wrong, we go uo stream

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9, 10);

        numbers.stream()
//                .map(e -> func1(e)) // is a one to one mapping
//                .map(e -> func2(e)) // is a one to many mapping
                .flatMap(e -> Stream.of(func2(e)))
                .forEach(System.out::println);

        // map one-to-one Stream<T> ==> Stream<Y>
        // map one-to-many Stream<T> ==> Stream<List<Y>>
        // flatMap one-to-many Stream<T> ==> Stream<Y> ???

        List<Integer> duplicates = Arrays.asList(1,2,3,4,5,1,2,3,4,5);

        // filter and map stay withing their swimlanes

        // reduce cuts across swimlanes. It bring the values together. It transforms a collection into a single value or a non-stream
        // Take the input coming in, take the first element, perform the operation, put the result out step forward,
        // the result becomes an input, take the next result, perform the operation, put the result out, take the next result
        //
        //       ||||||||||||||||||||||||||||||||||||||
        //      |     one stage       the next stage |
        //     |                    T               |
        //    |                   1                |       2           3           4
        //   |                  /                 |     /            /           /
        //  | R (input) 1 -> * -> R (output) 1 ->|    * -> 2 ->    * -> 6 ->   * -> 24
        // ||||||||||||||||||||||||||||||||||||||
        // an output for one stage is an input for the next stage
        //
        // reduce on Stream<T> takes two parameters:
        // first parameter is of typeT
        // second parameter is of type BiFunction<R, T, R> to produce a result of R
        // R means an input type, T means an element's type, R means the output type
        //
        // '/' means the filter blocks the value. '->' means the filter moves forward the value. The values mind their own
        // business. '+' means an available element. They stay withing their swimlanes. '----------' means a swimlane
        //
        // filter        map         reduce
        //                             0.0 is an initial value which is coming in
        //     values in some collection \
        // x1   /                         \
        // ---------- ----------           \
        // x2   ->       x2'          ->    +
        // ---------- ----------             \
        // x3   /                             \
        // ---------- ----------               \
        // x4   ->       x4'          ->        +


        // filter lets some values go and blocks others
        // filter: 0 <= number of elements in the output <= number of the input
        // parameter: Stream<T> filter takes Predicate<T>
        System.out.println(numbers.stream()
                .filter(e -> e % 2 == 0)
                .reduce(20, (a, b) -> a + b));

        System.out.println(numbers.stream()
                .filter(e -> e % 2 == 0)
                .mapToDouble(e -> e * 2.0)
                .sum());

        // map transforms values. It receives a function as a parameter that will take a value from the collection coming in
        // and returns the value into the stream which is going out. Input stream -> output stream
        // number of the output == number of the input
        // no guarantee on the type of the output with respect to the type of the input
        // parameter: Stream<T> map takes Function<T, R> to return Stream<R>
        System.out.println(numbers.stream()
                .filter(e -> e % 2 == 0)
                .map(e -> e * 2) // input type is Integer. The output type is Integer (stream)
//                .map(e -> e * 2.0) // input type is Integer. The output type is Double (stream)
                .reduce(20, (a, b) -> a + b));

        System.out.println(numbers.stream()
                .map(e -> e * 2.0)
                .reduce(0.0, (a, b) -> a + b));

        List<String> symbols = Arrays.asList("GOOG", "AAPL", "MSFT", "INTC");
        System.out.println(symbols.stream()
                .map(String::toLowerCase) // higher-order function
                .filter(symbol -> symbol.startsWith("G"))
                .collect(toList()));

        //double the even values and put them into a list

        // wrong way to do this
        List<Integer> doubleOfEven = new ArrayList<>();

        duplicates.stream()
                .filter(e -> e % 2 == 0)
                .map(e -> e * 2)
                .forEach(e -> doubleOfEven.add(e)); // doubleOfEven is a shared variable
        // mutability is Ok, sharing is nice, shared mutability is wrong. Friends don't let friends do shared mutation
        System.out.println(doubleOfEven); // do not do this

        // collect is a reduce operation. It lets us get data out of streams and into another form

        // right way to do this
        List<Integer> doubleOfEven2 = duplicates.stream()
                .filter(e -> e % 2 == 0)
                .map(e -> e * 2)
                .collect(toList());
        System.out.println(doubleOfEven2);

        System.out.println();
        System.out.println("toSet");
        Set<Integer> doubleOfEven3 = duplicates.stream()
                .filter(e -> e % 2 == 0)
                .map(e -> e * 2)
                .collect(toSet());
        System.out.println(doubleOfEven3);



        List<Person> people = createPeople();

        // create a Map with name and age as key, and the person as value. toMap returns a Collector that accumulates elements
        // into a Map whose keys and values are the result of applying the provided mapping functions to the input elements.
        System.out.println(people.stream()
                .collect(toMap(
                        // pass a lamda that transforms objects as a key
                        person -> person.getFirstName() + "-" + person.getAge(),
                        // pass person as a value
                        person -> person
                )));

        // groupingBy Returns a Collector implementing a "group by" operation on input elements of type T, grouping elements
        // according to a classification function, and returning the results in a Map.

        // given a list of people, create a map where their name is a key and value is all the people with that name
        // first, create an empty map. Then, go take a list, take the first element from the list (it's a person), get a name
        // of the person. If a key(a name of a person) has not been added to the map before, create an empty list, add this object (person)
        // to the empty list, do a put on the map where put is name of a person and object is the list we've created, get the second
        // person, get the value for the person if such a key is already in the map, add the second person to the already existed list
        // according to the key (person's name) and so on. Otherwise, just add a key and a value

        System.out.println(people.stream()
                .collect(groupingBy(Person::getFirstName))); // group elements by the first name. A key is the name and a value is
        // a list containing the people that have the same name

        System.out.println();
        System.out.println();
        // given a list of people, create a map where their name is a key and value is all the ages of people with that name
        System.out.println(people.stream()
                .collect(groupingBy(Person::getFirstName,
                        mapping(Person::getAge, toList())))); // names as a key and ages of the people as a value

        List<Integer> numbers2 = Arrays.asList(1,2,3,5,4,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20);
        // given an ordered list find the double of the first even number greater than 3

        // performance


        // 8 units of work
        int result = 0;
        for (int e : numbers) {
            if (e > 3 && e % 2 == 0) {
                result = e * 2;
                break;
            }
        }
        System.out.println(result);

        // 46 units of work
        numbers2.stream()
                .filter(e -> e > 3)
                .filter(e -> e % 2 == 0)
                .map(e -> e * 2)
                .findFirst();

        // streams are absolutely lazy. Lazy evaluation is possible only if the function don't have side effect. Computation
        // on the source data is only performed when the terminal operation is initiated, and source elements are consumed only
        // as needed
        numbers2.stream() // building a pipeline. Take one element and apply an entire sequence of computations on that element.
                // Only when we're done take the next element and apply the entire sequence on the second element and so on
                // sequence
                .filter(StreamOperations::isGT3) // whether the number is greater than 3. Intermediate operation
                .filter(StreamOperations::isEven) // go to this step if the previous one is true. Intermediate operation
                .map(StreamOperations::doubleIt) // double it. Intermediate operation
                .findFirst(); // get the result. Terminal operation

        // sized, ordered, non-distinct, non-sorted
        duplicates.stream()
                .filter(e -> e % 2 == 0)
                .forEach(System.out::println);

        // sized, ordered, non-distinct, sorted
        duplicates.stream()
                .filter(e -> e % 2 == 0)
                .sorted()
                .forEach(System.out::println);

        // sized, ordered, distinct, sorted
        duplicates.stream()
                .filter(e -> e % 2 == 0)
                .sorted()
                .distinct()
                .forEach(System.out::println);

        // infinite stream. It cannot exists without laziness. Laziness cannot exist without a side effect. Side effect
        // cannot exist without immutability. Start with 100, create a series 100, 101, 102, 103, ...
        Stream.iterate(100, e -> e + 1);

        // given a number k, and a count n, find the total of double of n even numbers starting with k,
        // where sqrt of each number > 20

    }

    private static int func1(int e) {
        return e * 2;
    }

    private static Object[] func2(int e) {
        return new Object[]{e - 1, e + 1};
    }

    public static List<Person> createPeople() {
        return Arrays.asList(
                new Person("Sara", Gender.FEMALE, 20),
        new Person("Brenda", Gender.FEMALE, 20),
        new Person("Sara", Gender.FEMALE, 22),
        new Person("Bob", Gender.MALE, 20),
        new Person("Paula", Gender.FEMALE, 32),
        new Person("Paul", Gender.MALE, 32),
        new Person("Jack", Gender.MALE, 2),
        new Person("Jack", Gender.MALE, 72),
        new Person("Jill", Gender.FEMALE, 12)
        );
    }

    public static boolean isGT3(int number) {
        System.out.println("isGT3 " + number);
        return number > 3;
    }

    public static boolean isEven(int number) {
        System.out.println("isEven " + number);
        return number % 2 == 0;
    }

    public static int doubleIt(int number) {
        System.out.println("doubleIt " + number);
        return number * 2;
    }

    public static int compute(int k, int n) {
//        int result = 0, index = k, count = 0;
//
//        while (count < n) {
//            if (index % 2 == 0 && Math.sqrt(index) > 20) {
//                result += index * 2;
//                count++;
//            }
//            index++;
//        }
//        return result;

        return Stream.iterate(k, e -> e + 1)// get an infinite stream starting with k. Unbounded, lazy
                .filter(e -> e % 2 == 0) // unbounded, lazy
                .filter(e -> Math.sqrt(e) > 20) // unbounded, lazy
                .mapToInt(e -> e * 2) // unbounded, lazy
                .limit(n) // sized, lazy
                .sum();
    }
}
