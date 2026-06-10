package views;

import test.Graph;
import test.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * View layer: generates an HTML page that visually displays a computational Graph.
 * Topics (prefix "T") are drawn as rectangles, Agents (prefix "A") as circles.
 * Directed arrows follow the edges stored in each Node.
 */
public class HtmlGraphWriter {

    /**
     * Returns a list of HTML lines representing a full page that draws the graph.
     *
     * @param graph the Graph (ArrayList of Node) built by Graph.createFromTopics()
     * @return list of HTML lines
     */
    public static List<String> getGraphHTML(Graph graph) {
        List<String> lines = new ArrayList<String>();

        // Build JS arrays for nodes and edges
        StringBuilder jsNodes = new StringBuilder("[");
        StringBuilder jsEdges = new StringBuilder("[");

        // Layout: place nodes in a circle
        int total = graph.size();
        int cx = 480, cy = 280, rx = 340, ry = 210;
        boolean firstNode = true;
        boolean firstEdge = true;

        // Map node name -> index for edge references
        java.util.HashMap<String, Integer> nameToIdx = new java.util.HashMap<String, Integer>();

        int idx = 0;
        for (Node node : graph) {
            nameToIdx.put(node.getName(), idx);
            double angle = 2 * Math.PI * idx / Math.max(total, 1);
            int x = (int)(cx + rx * Math.cos(angle));
            int y = (int)(cy + ry * Math.sin(angle));

            boolean isTopic = node.getName().startsWith("T");
            // Display label: strip the leading T or A prefix
            String label = node.getName().substring(1);
            // Last message value for topics
            String value = "";
            if (isTopic && node.getMessage() != null) {
                value = node.getMessage().asText;
            }

            if (!firstNode) jsNodes.append(",");
            jsNodes.append("{")
                   .append("\"id\":").append(idx).append(",")
                   .append("\"label\":\"").append(jsEsc(label)).append("\",")
                   .append("\"type\":\"").append(isTopic ? "topic" : "agent").append("\",")
                   .append("\"value\":\"").append(jsEsc(value)).append("\",")
                   .append("\"x\":").append(x).append(",")
                   .append("\"y\":").append(y)
                   .append("}");
            firstNode = false;
            idx++;
        }
        jsNodes.append("]");

        // Build edges from node.getEdges()
        idx = 0;
        for (Node node : graph) {
            int fromIdx = nameToIdx.get(node.getName());
            for (Node target : node.getEdges()) {
                Integer toIdx = nameToIdx.get(target.getName());
                if (toIdx == null) continue;
                if (!firstEdge) jsEdges.append(",");
                jsEdges.append("{\"from\":").append(fromIdx).append(",\"to\":").append(toIdx).append("}");
                firstEdge = false;
            }
            idx++;
        }
        jsEdges.append("]");

        // HTML page
        lines.add("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Computational Graph</title>");
        lines.add("<style>");
        lines.add("*{box-sizing:border-box;margin:0;padding:0;}");
        lines.add("body{background:#1e1e2e;display:flex;flex-direction:column;align-items:center;padding:12px;font-family:Arial,sans-serif;}");
        lines.add("h2{color:#89b4fa;margin-bottom:10px;font-size:1em;}");
        lines.add("canvas{background:#181825;border-radius:10px;box-shadow:0 4px 24px rgba(0,0,0,0.6);max-width:100%;}");
        lines.add("</style></head><body>");
        lines.add("<h2>Computational Graph</h2>");
        lines.add("<canvas id='c' width='960' height='560'></canvas>");
        lines.add("<script>");
        lines.add("const nodes=" + jsNodes + ";");
        lines.add("const edges=" + jsEdges + ";");
        lines.add("const canvas=document.getElementById('c');");
        lines.add("const ctx=canvas.getContext('2d');");
        lines.add("const TW=88,TH=34,AR=28;");
        lines.add("const TCOL='#89dceb',ACOL='#a6e3a1',ECOL='#f38ba8',TBRD='#74c7ec',ABRD='#40a02b';");
        lines.add("");
        lines.add("function nodePos(id){const n=nodes.find(n=>n.id===id);return n?{x:n.x,y:n.y}:{x:0,y:0};}");
        lines.add("");
        lines.add("function arrow(x1,y1,x2,y2){");
        lines.add("  const dx=x2-x1,dy=y2-y1,len=Math.sqrt(dx*dx+dy*dy);");
        lines.add("  if(len<1)return;");
        lines.add("  const ux=dx/len,uy=dy/len,gap=34;");
        lines.add("  const sx=x1+ux*gap,sy=y1+uy*gap,ex=x2-ux*gap,ey=y2-uy*gap;");
        lines.add("  ctx.beginPath();ctx.moveTo(sx,sy);ctx.lineTo(ex,ey);");
        lines.add("  ctx.strokeStyle=ECOL;ctx.lineWidth=2;ctx.stroke();");
        lines.add("  const a=Math.atan2(ey-sy,ex-sx);");
        lines.add("  ctx.beginPath();ctx.moveTo(ex,ey);");
        lines.add("  ctx.lineTo(ex-11*Math.cos(a-0.4),ey-11*Math.sin(a-0.4));");
        lines.add("  ctx.lineTo(ex-11*Math.cos(a+0.4),ey-11*Math.sin(a+0.4));");
        lines.add("  ctx.closePath();ctx.fillStyle=ECOL;ctx.fill();");
        lines.add("}");
        lines.add("");
        lines.add("function draw(){");
        lines.add("  ctx.clearRect(0,0,canvas.width,canvas.height);");
        lines.add("  edges.forEach(e=>{const f=nodePos(e.from),t=nodePos(e.to);arrow(f.x,f.y,t.x,t.y);});");
        lines.add("  nodes.forEach(n=>{");
        lines.add("    ctx.save();");
        lines.add("    if(n.type==='topic'){");
        lines.add("      ctx.fillStyle=TCOL;");
        lines.add("      ctx.beginPath();ctx.roundRect(n.x-TW/2,n.y-TH/2,TW,TH,5);ctx.fill();");
        lines.add("      ctx.strokeStyle=TBRD;ctx.lineWidth=2;ctx.stroke();");
        lines.add("      ctx.fillStyle='#1e1e2e';ctx.textAlign='center';ctx.textBaseline='middle';");
        lines.add("      ctx.font='bold 12px Arial';ctx.fillText(n.label,n.x,n.y-(n.value?6:0));");
        lines.add("      if(n.value){ctx.font='10px Arial';ctx.fillStyle='#555';ctx.fillText(n.value,n.x,n.y+8);}");
        lines.add("    }else{");
        lines.add("      ctx.fillStyle=ACOL;");
        lines.add("      ctx.beginPath();ctx.arc(n.x,n.y,AR,0,2*Math.PI);ctx.fill();");
        lines.add("      ctx.strokeStyle=ABRD;ctx.lineWidth=2;ctx.stroke();");
        lines.add("      ctx.fillStyle='#1e1e2e';ctx.textAlign='center';ctx.textBaseline='middle';");
        lines.add("      ctx.font='bold 11px Arial';ctx.fillText(n.label,n.x,n.y);");
        lines.add("    }");
        lines.add("    ctx.restore();");
        lines.add("  });");
        lines.add("}");
        lines.add("draw();");
        lines.add("</script></body></html>");

        return lines;
    }

    private static String jsEsc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }
}
