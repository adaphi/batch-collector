import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class BatchInline {

	public static <T> Collector<T, ?, Collection<Collection<T>>> batch(int batchSize)
	{
		BiConsumer<Collection<Collection<T>>,T> addToBatch = (a, t) -> {
			a.stream()
				.filter( c -> c.size() < batchSize )
				.findFirst()
				.orElseGet( () -> {ArrayList<T> l = new ArrayList<T>(); a.add(l); return l;} )
				.add(t);
		};

		return Collector.of(
			ArrayList<Collection<T>>::new,
			addToBatch,
			(left, right) -> {
				for (Collection<T> c : right) {
					for (T t : c) {
						addToBatch.accept(left, t);
					}
				};
				return left;
			}
		);
	}

	public static void main(String[] args)
	{
		IntStream.rangeClosed(1,100).boxed().collect(batch(7)).forEach(System.out::println);
	}

}
