package kr.co.inhatc.lms.study;

import kr.co.inhatc.lms.account.Account;
import kr.co.inhatc.lms.account.CurrentUser;
import kr.co.inhatc.lms.lecture.Lecture;
import kr.co.inhatc.lms.lecture.LectureRepository;
import kr.co.inhatc.lms.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final LectureService lectureService;
    private final StudyService studyService;
    private final StudyRepository studyRepository;
    private final LectureRepository lectureRepository;
    private final StudyValidator studyValidator;
    private final ModelMapper modelMapper;
    private void list(Account account, Model model) {
        model.addAttribute("lectureManagerOf",lectureRepository.findFirst20ByLecturerContaining(account));
        model.addAttribute("studentManagerOf",lectureRepository.findFirst20ByStudentContaining(account));
    }
    /**
     * <pre>
     *     <b>강의 생성</b>
     * </pre>
     * **/
    @GetMapping("/lecture/{path}/createStudy")
    public String createStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        Lecture lecture = lectureService.getLectureToUpdateStatus(path);
        list(account, model);
        model.addAttribute(lecture);
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/lecture/{path}/createStudy")
    public String createStudySubmit(@CurrentUser Account account, @PathVariable String path,
                                 @Valid StudyForm studyForm, Errors errors, Model model) {
        Lecture lecture = lectureService.getLectureToUpdateStatus(path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(lecture);
            return "study/form";
        }

        studyValidator.validate(studyForm,errors);
        if (errors.hasErrors()){
            return "study/form";
        }

        Study study = studyService.createStudy(modelMapper.map(studyForm, Study.class), lecture, account);
        return "redirect:/study/" + lecture.getEncodedPath() + "/events/" + study.getId();
    }

    @GetMapping("/study/{path}/events/{id}")
    public String viewStudy(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        Lecture lecture = lectureService.getLecture(path);
        model.addAttribute(studyRepository.findById(id).orElseThrow());
        model.addAttribute(lectureService.getLecture(path));
        List<Study> study = studyRepository.findByLectureOrderByStartDateTime(lecture);
        list(account, model);
        model.addAttribute(account);
        model.addAttribute("studys",study);
        return "study/view";
    }

    @GetMapping("/study/{path}/events")
    public String studyList(@CurrentUser Account account, @PathVariable String path, Model model,
                                  @PageableDefault(size = 4,direction = Sort.Direction.DESC)Pageable pageable) {
        Lecture lecture = lectureService.getLecture(path);
        Page<Study> studyPage = studyRepository.findByLectureOrderByStartDateTime(lecture,pageable);
        list(account, model);
        model.addAttribute("studyPage",studyPage);
        model.addAttribute(account);
        model.addAttribute(lecture);
        return "study/events";
    }

    @GetMapping("/study/{path}/events/{id}/edit")
    public String editStudy(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        Lecture lecture = lectureService.getLectureToUpdateStatus(path);
        Study study = studyRepository.findById(id).orElseThrow();
        list(account, model);
        model.addAttribute(lecture);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyForm.class));

        return "study/update-form";
    }

    @PostMapping("/study/{path}/events/{id}/edit")
    public String editStudySubmit(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, @Valid StudyForm studyForm, Errors errors, Model model) {
        Lecture lecture = lectureService.getLectureToUpdateStatus(path);
        Study study = studyRepository.findById(id).orElseThrow();

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(lecture);
            model.addAttribute(study);
            return "study/update-form";
        }

        studyService.editStudy(study, studyForm);

        return "redirect:/study/" + lecture.getEncodedPath() + "/events/" + study.getId();
    }

    @PostMapping("/study/{path}/events/{id}")
    public String deleteStudy(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Lecture lecture = lectureService.getLectureToUpdateStatus(path);
        studyService.deleteStudy(studyRepository.findById(id).orElseThrow());
        return "redirect:/study/" + lecture.getEncodedPath() + "/events";
    }
}
