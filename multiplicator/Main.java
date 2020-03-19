package multiplicator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.*;

public class Main
{
    private static final int weight = 1;
    private static final int startToken = 1;
    private static final int NUMBER_OF_THREADS = 4;

    private static Collection<Transition<String>> transitions;
    private static Collection<Transition<String>> outstandingTransition;

    private static PetriNet<String> petriNet;
    private static String resultPlace = "Result";

    public static class MultiplicatorThread implements Runnable {

        private int number;

        public MultiplicatorThread(int number)
        {
            this.number = number;
        }

        @Override
        public void run()
        {
            int howMany = 0;

            try
            {
                while (true)
                {
                    petriNet.fire(transitions);
                    howMany++;
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                System.out.println("Thread number " + number + " fired " +
                        howMany + " number of times");
            }
        }
    }

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Thread> threads = new ArrayList<>();

        int a = scanner.nextInt();
        int b = scanner.nextInt();

        initialize(a, b);

        for (int i = 0; i < NUMBER_OF_THREADS; i++)
        {
            Thread thread = new Thread(new MultiplicatorThread(i));
            threads.add(thread);
            thread.start();
        }

        try
        {
            petriNet.fire(outstandingTransition);

            for (Thread thread: threads)
            {
                thread.interrupt();
            }

            Set<Map<String, Integer>> mappingSet = petriNet.reachable(outstandingTransition);

            for (Map<String, Integer> mapping: mappingSet)
            {
                if (mapping.get(resultPlace) != null)
                {
                    System.out.println(mapping.get(resultPlace));
                }
                else
                {
                    System.out.println(0);
                }
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();

            for (Thread thread: threads)
            {
                thread.interrupt();
            }

            System.out.println("Main thread interrupted");
        }
    }


    private static void initialize(int a, int b)
    {
        String aPlace = "A", bPlace = "B", tempBPlace = "TempB",
                tokenMove1Place = "tokenMove1",  tokenMove2Place = "tokenMove2";

        HashMap<String, Integer> mapOfPlaces = new HashMap<>();
        mapOfPlaces.put(tokenMove1Place, startToken);

        if (a > 0)
        {
            mapOfPlaces.put(aPlace, a);
        }
        if (b > 0)
        {
            mapOfPlaces.put(bPlace, b);
        }

        petriNet = new PetriNet<>(mapOfPlaces, true);

        Transition<String> vTransition = new Transition<>(
                Map.of(tempBPlace, weight, tokenMove1Place, weight),
                null,
                null,
                Map.of(bPlace, weight, tokenMove1Place, weight)
        );
        Transition<String> pTransition = new Transition<>(
                Map.of(bPlace, weight, tokenMove2Place, weight),
                null,
                null,
                Map.of(tempBPlace, weight, tokenMove2Place, weight,
                        resultPlace, weight)
        );
        Transition<String> tokenMove1Transition = new Transition<>(
                Map.of(tokenMove1Place, weight, aPlace, weight),
                null,
                Collections.singletonList(tempBPlace),
                Map.of(tokenMove2Place, weight)
        );
        Transition<String> tokenMove2Transition = new Transition<>(
                Map.of(tokenMove2Place, weight),
                null,
                Collections.singletonList(bPlace),
                Map.of(tokenMove1Place, weight)
        );

        transitions = new ArrayList<>();
        transitions.add(vTransition);
        transitions.add(pTransition);
        transitions.add(tokenMove1Transition);
        transitions.add(tokenMove2Transition);

        Transition<String> outstanding = new Transition<>(
                Map.of(tokenMove1Place, weight),
                null,
                Collections.singletonList(aPlace),
                null
        );

        outstandingTransition = new ArrayList<>();
        outstandingTransition.add(outstanding);
    }
}
