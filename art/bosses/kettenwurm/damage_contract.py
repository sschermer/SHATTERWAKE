"""Kettenwurm damage-state contract (Blender preview; Android remains unchanged).

HP thresholds are per node, never the total boss HP. Four monotone states:
0 intact >2/3, 1 dented (1/3,2/3], 2 sparking (0,1/3], 3 destroyed <=0.
"""
NODE_TYPES=tuple([0]+[3 if i%5==0 else 2 if i%4==0 else 4 if i%3==0 else 1 for i in range(1,12)])
TYPE_NAMES=("Kopf","Waffenmodul","Trennkupplung","Reaktorkern","Energiespeicher")
STATE_NAMES=("Intakt","Leicht verbeult","Stark verbeult / Funken","Explosion / zerstört")

def damage_state(hp,max_hp):
    if max_hp<=0:
        raise ValueError("max_hp must be positive")
    if hp<=0:
        return 3
    if hp*3<=max_hp:
        return 2
    if hp*3<=max_hp*2:
        return 1
    return 0
