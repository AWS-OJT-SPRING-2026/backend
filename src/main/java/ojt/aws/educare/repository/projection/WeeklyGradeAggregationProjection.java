package ojt.aws.educare.repository.projection;

public interface WeeklyGradeAggregationProjection {
    Integer getDayOfWeek();
    Long getHocSinhGioiKha();
    Long getHocSinhYeuKem();
    Long getTongBaiCham();
}

