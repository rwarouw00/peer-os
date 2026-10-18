package myxul.service;

/**
 * A Java object a XUL window can ask for with
 * <?xul-service ScriptName myxul.service.ClassName?>. Its scripts see it as a
 * global called ScriptName. Implementations have a public constructor taking
 * the XulWindow, and only public methods are reachable from scripts.
 */
public interface XulService {
}
