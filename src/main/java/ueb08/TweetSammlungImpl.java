package ueb08;

import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class TweetSammlungImpl implements TweetSammlung {
	private List<String> tweets = new LinkedList<>();
	private Map<String, Integer> index = new TreeMap<>();
	private Set<String> stop = new TreeSet<>();

	@Override
	public void ingest(String tweet) {
		tweets.add(tweet);

		for (String tok : TweetSammlung.tokenize(tweet)) {
			if (stop.contains(tok))
				continue;

			// increase count for word
			index.merge(tok, 1, (a, b) -> a + b);
		}
	}

	@Override
	public Iterator<String> vocabIterator() {
		// TreeSet.iterator sortiert aufsteigend!
		// https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html#keySet--
		return index.keySet().iterator();
	}

	@Override
	public Iterator<String> topHashTags() {
		// filtern...
		List<Pair<String, Integer>> help = new LinkedList<>();
		for (Map.Entry<String, Integer> e : index.entrySet()) {
			if (e.getKey().startsWith("#"))
				help.add(Pair.of(e.getKey(), e.getValue()));
		}

		// sortieren, aufsteigend
		help.sort(new Comparator<Pair<String, Integer>>() {
			@Override
			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}
		});

		// uebertragen in nur-key
		List<String> list = new LinkedList<>();
		for (Map.Entry<String, Integer> e : help)
			list.add(e.getKey());

		return list.iterator();
	}

	@Override
	public Iterator<String> topWords() {
		// like above, but using streams and lambda
		return index.entrySet().stream()
				// remove hashtags
				.filter(e -> Character.isAlphabetic(e.getKey().charAt(0)))
				// sortiere nach raw-count aufsteigend
				.sorted((e1, e2) -> e2.getValue() - e1.getValue())
				// jetzt nur den key behalten
				.map(Map.Entry::getKey).iterator();
	}

	public Iterator<Pair<String, Integer>> topTweets() {
		List<Pair<String, Integer>> list = new LinkedList<>();
		for (String tw : tweets) {
			int summe = 0;
			for (String tok : TweetSammlung.tokenize(tw)) {
				if (stop.contains(tok))
					continue;
				summe += index.get(tok);
			}
			list.add(Pair.of(tw, summe));
		}

		list.sort(new Comparator<Pair<String, Integer>>() {
			@Override
			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
				return Integer.compare(o2.getRight(), o1.getRight());
			}
		});

		return list.iterator();
	}

	@Override
	public void setStopwords(File file) throws FileNotFoundException {
		Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));
		while (scanner.hasNext())
			stop.add(scanner.next());
	}
}