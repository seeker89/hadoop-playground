out.mpg: frame_1.png 
	convert frame_1.png  -antialias -quality 95% out.mpg
frame_1.png: Subway_scene_finished.blend
	blender -b Subway_scene_finished.blend -o //frame_# -F PNG -x 1 -f 1
