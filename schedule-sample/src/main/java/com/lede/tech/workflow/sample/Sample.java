package com.lede.tech.workflow.sample;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lede.tech.workflow.core.engine.Engine;
import com.lede.tech.workflow.core.engine.bean.ProcessTemplate;
import com.lede.tech.workflow.core.engine.bean.TemplateBean;
import com.lede.tech.workflow.core.model.graph.Edge;
import com.lede.tech.workflow.core.model.petri.PetriNet;
import com.lede.tech.workflow.core.model.petri.Place;
import com.lede.tech.workflow.core.model.petri.Transition;
import com.lede.tech.workflow.sample.graphviz.GraphViz;

@Service
public class Sample
{
	@Autowired
	ProcessTemplate sampleTemplate3;

	@PostConstruct
	private void init()
	{
		Engine.start();
		Engine.removeFinishedInstance();
		Engine.initInstance(sampleTemplate3);

		//createGraph();
	}

	void createGraph()
	{
		GraphViz gv = new GraphViz();
		gv.addln(getPetriNetDot());
		gv.increaseDpi(); // 106 dpi

		String type = "gif";
		String repesentationType = "dot";
		File out = new File("E:/gitcode/TaskScheduler/schedule-sample/src/main/webapp/petriNet." + type); // Linux
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, repesentationType), out);

	}

	public String getPetriNetDot()
	{
		StringBuffer sb = new StringBuffer("digraph petriNet{\nrankdir=LR;\n");

		TemplateBean templateBean = Engine.getContainer().getTemplate(sampleTemplate3);
		PetriNet petriNet = templateBean.getProcess();
		Transition[] transitions = petriNet.getTransitions();
		Place[] places = petriNet.getPlaces();

		for (Transition t : transitions)
		{
			sb.append(t.getName() + " [shape=\"record\"];\n");
		}
		for (Place p : places)
		{
			sb.append(p.getName().replace("-", "_") + " [shape=\"circle\"];\n");
		}

		for (Transition t : transitions)
		{
			Edge[] edges = t.getTarget();
			for (Edge edge : edges)
			{
				String desc = edge.getDescriptor() == null ? "" : edge.getDescriptor();
				sb.append(t.getName() + " -> " + edge.getTarget().getName().replace("-", "_") + " [label=\"" + desc
						+ " " + edge.getName() + "\"]" + ";\n");
			}
		}

		for (Place p : places)
		{
			Edge[] edges = p.getTarget();
			for (Edge edge : edges)
			{
				String desc = edge.getDescriptor() == null ? "" : edge.getDescriptor();
				sb.append(p.getName().replace("-", "_") + " -> " + edge.getTarget().getName() + " [label=\"" + desc
						+ " " + edge.getName() + "\"]" + ";\n");
			}
		}

		sb.append("}");

		return sb.toString();

	}
}
