package com.phasip.lectureview;

import com.phasip.lectureview.list.LectureListView;
import com.phasip.lectureview.listhandlers.CourseListHandler;
import com.phasip.lectureview.listhandlers.LectureListHandler;
import com.phasip.lectureview.listhandlers.ListHandler;
import com.phasip.lectureview.listhandlers.PlayListHandler;
import com.phasip.lectureview.listhandlers.SubjectsListHandler;
import com.phasip.lectureview.listhandlers.TopicListHandler;


public enum Patterns {
	COURSES {
		@Override
		public ListHandler launchHandler(LectureListView l, LectureViewer a, Link link) {
			return new CourseListHandler(l, a, link);
		}
	},
	RETRY {
		@Override
		public ListHandler launchHandler(LectureListView l, LectureViewer a, Link link) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	PLAY {
		@Override
		public ListHandler launchHandler(LectureListView l, LectureViewer a, Link link) {
			return new PlayListHandler(l,a, link);
		}
	},
	SUBJECTS {
		public ListHandler launchHandler(LectureListView l,LectureViewer a,Link link) {
			return new SubjectsListHandler(l,a);
		}
	},
	TOPICS {
		@Override
		public ListHandler launchHandler(LectureListView l, LectureViewer a, Link link) {
			return new TopicListHandler(l,a,link);
		}
	},
	LECTURES {
		@Override
		public ListHandler launchHandler(LectureListView l, LectureViewer a, Link link) {
			return new LectureListHandler(l, a, link);
		}
	};
	public abstract ListHandler launchHandler(LectureListView l,LectureViewer a,Link link);
}

