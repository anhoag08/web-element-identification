import fasttext.util

ft = fasttext.load_model('cc.en.5.bin')

print(ft["dogs"])

print(ft.get_word_vector("username"))

import numpy as np

cos_similarity = lambda a, b: np.dot(a, b)/(np.linalg.norm(a)*np.linalg.norm(b))

x, y = np.array(ft["dogs"]), np.array(ft["cars"])
print(cos_similarity(x,y))