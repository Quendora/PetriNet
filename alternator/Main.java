package alternator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.*;

public class Main
{
    private static final int weight = 1;
    private static final int startToken = 1;

    private static PetriNet<String> petriNet;

    private static class AlternatorThread implements Runnable {

        private Collection<Transition<String>> transitions;
        private String name;

        public AlternatorThread(Collection<Transition<String>> transitions, String name)
        {
            this.transitions = transitions;
            this.name = name;
        }

        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    petriNet.fire(transitions);

                    System.out.print(name);
                    System.out.print(".");

                    petriNet.fire(transitions);
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args)
    {
        String aInPlace = "aIn", bInPlace = "bIn", cInPlace = "cIn",
                aOutPlace = "aOut", bOutPlace = "bOut", cOutPlace = "cOut",
                aCheckPlace = "aCheck", bCheckPlace = "bCheck",
                cCheckPlace = "cCheck", middle = "middle";

        petriNet = new PetriNet<>(
                Map.of(aOutPlace, startToken, bOutPlace, startToken,
                        cOutPlace, startToken, middle, startToken),
                true
        );

        Collection<Transition<String>> ATransitions = initializeThreadTransitions(
                aOutPlace, aCheckPlace, aInPlace, bCheckPlace, cCheckPlace, middle
        );
        Collection<Transition<String>> BTransitions = initializeThreadTransitions(
                bOutPlace, bCheckPlace, bInPlace, aCheckPlace, cCheckPlace, middle
        );
        Collection<Transition<String>> CTransitions = initializeThreadTransitions(
                cOutPlace, cCheckPlace, cInPlace, aCheckPlace, bCheckPlace, middle
        );

        Collection<Transition<String>> transitions = getAllTransitions(
                ATransitions, BTransitions, CTransitions
        );

        Set<Map<String, Integer>> availableMarkings = petriNet.reachable(transitions);
        System.out.println(availableMarkings.size());

        checkSafety(availableMarkings);

        List<Thread> threads = createThreads(ATransitions, BTransitions,
                CTransitions);

        try
        {

            for (Thread thread: threads)
            {
                thread.start();
            }

            Thread.sleep(30000);

            for (Thread thread: threads)
            {
                thread.interrupt();
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

    private static ArrayList<Thread> createThreads(Collection<Transition<String>> ATransitions,
                                                   Collection<Transition<String>> BTransitions,
                                                   Collection<Transition<String>> CTransitions)
    {
        ArrayList<Thread> threads = new ArrayList<>();

        threads.add(new Thread(new AlternatorThread(ATransitions, "A")));
        threads.add(new Thread(new AlternatorThread(BTransitions, "B")));
        threads.add(new Thread(new AlternatorThread(CTransitions, "C")));

        return threads;
    }

    private static void checkSafety(Set<Map<String, Integer>> availableMarkings)
    {
        for (Map<String, Integer> marking : availableMarkings)
        {
            int howManyThreadsInCS = 0;

            for (Map.Entry<String, Integer> entry : marking.entrySet())
            {
                String place = entry.getKey();

                if (place.equals("aOut") || place.equals("bOut") || place.equals("cOut"))
                {
                    howManyThreadsInCS++;
                }
            }

            assert (howManyThreadsInCS <= 1);
        }
    }

    private static Collection<Transition<String>> initializeThreadTransitions(
            String tOutPlace, String tCheckPlace, String tInPlace,
            String other1CheckPlace, String other2CheckPlace, String middle)
    {
        Transition<String> aInTransition = new Transition<>(
                Map.of(tOutPlace, weight, middle, weight),
                null,
                Collections.singletonList(tCheckPlace),
                Map.of(tInPlace, weight)
        );

        Transition<String> aOutTransition = new Transition<>(
                Map.of(tInPlace, weight),
                Arrays.asList(other1CheckPlace, other2CheckPlace),
                null,
                Map.of(tOutPlace, weight, middle, weight, tCheckPlace, weight)
        );

        return Arrays.asList(aInTransition, aOutTransition);
    }

    private static Collection<Transition<String>> getAllTransitions(
            Collection<Transition<String>> ATransitions,
            Collection<Transition<String>> BTransitions,
            Collection<Transition<String>> CTransitions)
    {
        Collection<Transition<String>> transitions = new ArrayList<>();

        transitions.addAll(ATransitions);
        transitions.addAll(BTransitions);
        transitions.addAll(CTransitions);

        return transitions;
    }
}
