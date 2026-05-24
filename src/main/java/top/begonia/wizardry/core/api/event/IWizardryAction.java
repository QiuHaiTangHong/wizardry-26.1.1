package top.begonia.wizardry.core.api.event;

@FunctionalInterface
public interface IWizardryAction {
    void execute(Object context);
}
