from functionalLib import compose
from random import choice 
import os

movies = ["initial_D_stage_one", 
          "initial_D_stage_two", 
          "initial_D_stage_three", 
          "initial_D_extra_stage",
          "initial_D_super_stage",
          "initial_D_train_stage",
          "initial_D_spitfire_stage"]


rooms = 9

day = lambda : compose(list, map)(lambda d: (choice(movies), d, d+1), compose(list)(range(8,22,1))) 
screenings = compose(list, map)(lambda f: f(), 30 * [day])

pwd = os.getcwd()
dataFolder = pwd + "/data"

isExist = os.path.exists(dataFolder)
if not isExist:
   os.makedirs(dataFolder)


for (day,i) in zip(screenings,list(range(1,31))):
    f = open(dataFolder + f'/day_{i}', 'a')
    for (name, start, end) in day:
        f.write(f'{name} {start} {end}\n')
    f.close() 
    

whoah3whoah&myeurohero
