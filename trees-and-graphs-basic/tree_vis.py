
from graphviz import Digraph

def visualize_tree(node):
    def add_nodes_edges(node, dot=None):
        # Create Digraph object on first call
        if dot is None:
            dot = Digraph()
            dot.node(str(node.val))

        # Add left child
        if node.left:
            dot.node(str(node.left.val))
            dot.edge(str(node.val), str(node.left.val))
            dot = add_nodes_edges(node.left, dot=dot)

        # Add right child
        if node.right:
            dot.node(str(node.val))
            dot.edge(str(node.val), str(node.right.val))
            dot = add_nodes_edges(node.right, dot=dot)

        return dot

    # Generate Digraph and render it
    dot = add_nodes_edges(node)
    dot.render('tree.gv', view=True)  # This will create and open a .gv file
