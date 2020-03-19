package petrinet;

import java.util.*;
import java.util.concurrent.Semaphore;

public class PetriNet<T>
{
    private Map<T, Integer> initialPlaces;

    private Semaphore mutex;
    private Deque<ThreadPair<T>> fireQueue;


    public PetriNet(Map<T, Integer> initial, boolean fair)
    {
        initialPlaces = new HashMap<>(initial);
        mutex = new Semaphore(1, fair);
        fireQueue = new LinkedList<>();
    }


    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions)
    {
        Set<Map<T, Integer>> reachableMarking = new HashSet<>();
        Stack<Map<T, Integer>> markingsToAnalise = new Stack<>();

        try
        {
            mutex.acquire();

            markingsToAnalise.add(getCopyOfPlaces(initialPlaces));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted");
        }
        finally
        {
            mutex.release();
        }


        while (!markingsToAnalise.empty())
        {
            Map<T, Integer> marking = markingsToAnalise.pop();
            reachableMarking.add(marking);

            for (Transition<T> transition: transitions)
            {
                if (transition.isEnabled(marking))
                {
                    HashMap<T, Integer> markingCopy = new HashMap<>(marking);
                    fireSingleTransition(transition, markingCopy);

                    if (!reachableMarking.contains(markingCopy) &&
                        markingsToAnalise.search(markingCopy) == -1)
                    {
                        markingsToAnalise.add(markingCopy);
                    }
                }
            }
        }

        return reachableMarking;
    }


    public Transition<T> fire(Collection<Transition<T>> transitions)
            throws InterruptedException
    {
        mutex.acquire();

        Transition<T> transition =
                checkIfAnyTransitionIsEnabled(transitions);

        try
        {
            if (transition == null)
            {
                ThreadPair<T> threadPair = new ThreadPair<>(transitions);
                fireQueue.push(threadPair);

                mutex.release();
                threadPair.getSemaphore().acquire();

                transition = checkIfAnyTransitionIsEnabled(transitions);
            }

            fireSingleTransition(transition, initialPlaces);
        }
        catch (InterruptedException e)
        {
            throw new InterruptedException();
        }
        finally
        {
            wakeUpThreadOrReleaseMutex();
        }

        return transition;
    }

    private void wakeUpThreadOrReleaseMutex()
    {
        ThreadPair threadPairToRemove = null;

        for (ThreadPair<T> threadPair: fireQueue)
        {
            Transition<T> threadEnabledTransition =
                    checkIfAnyTransitionIsEnabled(threadPair.getTransitions());

            if (threadEnabledTransition != null)
            {
                threadPairToRemove = threadPair;
                break;
            }
        }

        if (threadPairToRemove != null)
        {
            fireQueue.remove(threadPairToRemove);
            threadPairToRemove.getSemaphore().release();
        }
        else
        {
            mutex.release();
        }
    }

    private Transition<T> checkIfAnyTransitionIsEnabled(
            Collection<Transition<T>> transitions)
    {
        for (Transition<T> transition: transitions)
        {
            if (transition.isEnabled(initialPlaces))
            {
                return transition;
            }
        }

        return null;
    }


    private void fireSingleTransition(Transition<T> transition,
                                     Map<T, Integer> marking)
    {
        fireInputArcs(transition.getInput(), marking);

        fireResetArcs(transition.getReset(), marking);

        fireOutputArcs(transition.getOutput(), marking);
    }

    private void fireInputArcs(HashMap<T, Integer> input, Map<T, Integer> marking)
    {
        for (Map.Entry<T, Integer> inputEntry: input.entrySet())
        {
            T place =  inputEntry.getKey();
            Integer weight = inputEntry.getValue();
            Integer tokens = marking.get(place);

            if (tokens - weight == 0)
            {
                marking.remove(place);
            }
            else
            {
                marking.replace(place, tokens - weight);
            }
        }
    }

    private void fireResetArcs(Collection<T> reset, Map<T, Integer> marking)
    {
        for (T place: reset)
        {
            marking.remove(place);
        }
    }

    private void fireOutputArcs(HashMap<T, Integer> output, Map<T, Integer> marking)
    {
        for (Map.Entry<T, Integer> outputEntry: output.entrySet())
        {
            T place =  outputEntry.getKey();
            Integer weight = outputEntry.getValue();

            if (marking.containsKey(place))
            {
                Integer tokens = marking.get(place);
                marking.replace(place, tokens + weight);
            }
            else
            {
                marking.put(place, weight);
            }
        }
    }

    private Map<T, Integer> getCopyOfPlaces(Map<T, Integer> places)
    {
        return new HashMap<>(places);
    }
}