"""Pure-Python deterministic visual-damage contract checks: no Android side effects."""
from pathlib import Path
import sys
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/"art/bosses/kettenwurm"))
from damage_contract import NODE_TYPES, TYPE_NAMES, STATE_NAMES, damage_state
assert NODE_TYPES==(0,1,1,4,2,3,4,1,2,4,3,1)
assert set(NODE_TYPES)==set(range(5)) and len(NODE_TYPES)==12
assert len(TYPE_NAMES)==5 and len(STATE_NAMES)==4
for max_hp in (39,57,135,185,300):
    assert damage_state(max_hp,max_hp)==0
    assert damage_state(0,max_hp)==3
    assert damage_state(-3,max_hp)==3
    assert damage_state(.01,max_hp)==2
    assert damage_state(max_hp/3,max_hp)==2
    assert damage_state(max_hp/3+.0001,max_hp)==1
    assert damage_state(max_hp*2/3,max_hp)==1
    assert damage_state(max_hp*2/3+.0001,max_hp)==0
    states=[damage_state(max_hp*(1-i/100),max_hp) for i in range(101)]
    assert states==sorted(states)
try:
    damage_state(5,0)
    assert False
except ValueError:
    pass
print("PASS: 12 nodes / 5 types / 4 visual damage thresholds / deterministic boundary regression")
