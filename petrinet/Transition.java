package petrinet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Transition<T> {

    private HashMap<T, Integer> input;
    private ArrayList<T> reset;
    private ArrayList<T> inhibitor;
    private HashMap<T, Integer> output;

    public Transition(Map<T, Integer> input, Collection<T> reset,
                      Collection<T> inhibitor, Map<T, Integer> output)
    {
        this.input = (input == null) ? new HashMap<>() : new HashMap<>(input);
        this.output = (output == null) ? new HashMap<>() : new HashMap<>(output);
        this.reset = (reset == null) ? new ArrayList<>() : new ArrayList<>(reset);
        this.inhibitor = (inhibitor == null) ? new ArrayList<>() : new ArrayList<>(inhibitor);
    }

    public Transition(Transition<T> transition)
    {
        this.input = (transition.getInput() == null) ?
                null : new HashMap<>(transition.getInput());
        this.output = (transition.getOutput() == null) ?
                null : new HashMap<>(transition.getOutput());
        this.reset = (transition.getReset() == null) ?
                null : new ArrayList<>(transition.getReset());
        this.inhibitor = (transition.getInhibitor() == null) ?
                null : new ArrayList<>(transition.getInhibitor());
    }

    public HashMap<T, Integer> getInput()
    {
        return input;
    }

    public ArrayList<T> getReset()
    {
        return reset;
    }

    public ArrayList<T> getInhibitor()
    {
        return inhibitor;
    }

    public HashMap<T, Integer> getOutput()
    {
        return output;
    }

    public boolean isEnabled(Map<T, Integer> initialPlaces)
    {
        return checkIfInputArcsAreEnabled(initialPlaces) &&
                checkIfInhibitorArcsAreEnabled(initialPlaces);
    }

    private boolean checkIfInputArcsAreEnabled(Map<T, Integer> initialPlaces)
    {
        for (Map.Entry<T, Integer> inputEntry: input.entrySet())
        {
            T place = inputEntry.getKey();

            if (initialPlaces.containsKey(place))
            {
                Integer tokens = initialPlaces.get(place);
                Integer weight = inputEntry.getValue();

                if (tokens < weight)
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    private boolean checkIfInhibitorArcsAreEnabled(Map<T, Integer> initialPlaces)
    {
        for (T inhibitor: this.inhibitor)
        {
            if (initialPlaces.containsKey(inhibitor))
            {
                return false;
            }
        }

        return true;
    }

}