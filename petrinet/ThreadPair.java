package petrinet;

import java.util.Collection;
import java.util.concurrent.Semaphore;

public class ThreadPair<T>
{
    private Collection<Transition<T>> transitions;
    private Semaphore semaphore;

    public ThreadPair(Collection<Transition<T>> transitions)
    {
        this.transitions = transitions;
        semaphore = new Semaphore(0);
    }

    public Collection<Transition<T>> getTransitions()
    {
        return transitions;
    }

    public Semaphore getSemaphore()
    {
        return semaphore;
    }
}
