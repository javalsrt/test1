package com.znxsgl.student;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.znxsgl.student.model.QuizQuestion;

import java.util.List;

public class QuizPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CHOICE = 0;
    private static final int TYPE_JUDGE = 1;
    private static final int TYPE_ANALYSIS = 2;
    private static final int TYPE_FILL = 3;

    private final List<QuizQuestion> questions;
    private OnAnswerListener listener;

    public interface OnAnswerListener {
        void onAnswered(int position, String answer);
        void onAutoSkip(int position); // 超时未答自动跳过
    }

    public QuizPagerAdapter(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public void setOnAnswerListener(OnAnswerListener l) { this.listener = l; }

    @Override
    public int getItemViewType(int pos) {
        String t = questions.get(pos).getQuestionType();
        if ("判断".equals(t)) return TYPE_JUDGE;
        if ("解析".equals(t)) return TYPE_ANALYSIS;
        if ("填空".equals(t)) return TYPE_FILL;
        return TYPE_CHOICE;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (type == TYPE_JUDGE) return new JudgeVH(inf.inflate(R.layout.item_quiz_judge, parent, false));
        if (type == TYPE_ANALYSIS) return new AnalysisVH(inf.inflate(R.layout.item_quiz_analysis, parent, false));
        if (type == TYPE_FILL) return new FillVH(inf.inflate(R.layout.item_quiz_fill, parent, false));
        return new ChoiceVH(inf.inflate(R.layout.item_quiz_choice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        QuizQuestion q = questions.get(pos);
        if (h instanceof ChoiceVH) bindChoice((ChoiceVH) h, q, pos);
        else if (h instanceof JudgeVH) bindJudge((JudgeVH) h, q, pos);
        else if (h instanceof AnalysisVH) bindAnalysis((AnalysisVH) h, q, pos);
        else if (h instanceof FillVH) bindFill((FillVH) h, q, pos);
    }

    @Override
    public int getItemCount() { return questions.size(); }

    // ===== 选择题 =====
    private void bindChoice(ChoiceVH vh, QuizQuestion q, int pos) {
        vh.tvTag.setText("📝 单选题");
        vh.tvNum.setText((pos + 1) + "/" + questions.size());
        vh.tvQuestion.setText(q.getQuestion());
        vh.llOptions.removeAllViews();

        List<String> options = q.getOptions();
        if (options != null) {
            for (int i = 0; i < options.size(); i++) {
                String optText = options.get(i);
                boolean selected = optText.equals(q.getUserAnswer());

                TextView tv = new TextView(vh.itemView.getContext());
                tv.setText(optText.replace("*", ""));
                tv.setTextSize(15);
                tv.setTextColor(selected ? Color.WHITE : Color.parseColor("#1D1D1F"));
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setPadding(24, 16, 24, 16);

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(12);
                bg.setColor(selected ? Color.parseColor("#0A84FF") : Color.parseColor("#F5F5F7"));
                tv.setBackground(bg);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 12);
                tv.setLayoutParams(lp);

                final String answer = optText;
                tv.setOnClickListener(v -> {
                    q.setUserAnswer(answer);
                    q.setModifiedCount(q.getModifiedCount() + 1);
                    notifyItemChanged(pos);
                    if (listener != null) listener.onAnswered(pos, answer);
                });

                vh.llOptions.addView(tv);
            }
        }
    }

    // ===== 判断题 =====
    private void bindJudge(JudgeVH vh, QuizQuestion q, int pos) {
        vh.tvTag.setText("⚡ 判断题");
        vh.tvNum.setText((pos + 1) + "/" + questions.size());
        vh.tvQuestion.setText(q.getQuestion());

        vh.btnTrue.setOnClickListener(v -> {
            q.setUserAnswer("正确");
            if (listener != null) listener.onAnswered(pos, "正确");
        });
        vh.btnFalse.setOnClickListener(v -> {
            q.setUserAnswer("错误");
            if (listener != null) listener.onAnswered(pos, "错误");
        });
    }

    // ===== 解析题 =====
    private void bindAnalysis(AnalysisVH vh, QuizQuestion q, int pos) {
        vh.tvTag.setText("✍️ 解析题");
        vh.tvNum.setText((pos + 1) + "/" + questions.size());
        vh.tvQuestion.setText(q.getQuestion());
        vh.etAnswer.setText(q.getUserAnswer() != null ? q.getUserAnswer() : "");
        vh.btnSubmit.setOnClickListener(v -> {
            String ans = vh.etAnswer.getText().toString().trim();
            if (ans.isEmpty()) return;
            q.setUserAnswer(ans);
            if (listener != null) listener.onAnswered(pos, ans);
        });
    }

    // ===== 填空题 =====
    private void bindFill(FillVH vh, QuizQuestion q, int pos) {
        vh.tvTag.setText("🔤 填空题");
        vh.tvNum.setText((pos + 1) + "/" + questions.size());
        vh.tvQuestion.setText(q.getQuestion());
        vh.etAnswer.setText(q.getUserAnswer() != null ? q.getUserAnswer() : "");
        vh.btnSubmit.setOnClickListener(v -> {
            String ans = vh.etAnswer.getText().toString().trim();
            if (ans.isEmpty()) return;
            q.setUserAnswer(ans);
            if (listener != null) listener.onAnswered(pos, ans);
        });
    }

    // ===== ViewHolders =====
    static class ChoiceVH extends RecyclerView.ViewHolder {
        TextView tvTag, tvNum, tvQuestion;
        LinearLayout llOptions;
        ChoiceVH(View v) {
            super(v);
            tvTag = v.findViewById(R.id.tv_quiz_tag);
            tvNum = v.findViewById(R.id.tv_quiz_num);
            tvQuestion = v.findViewById(R.id.tv_quiz_question);
            llOptions = v.findViewById(R.id.ll_options);
        }
    }
    static class JudgeVH extends RecyclerView.ViewHolder {
        TextView tvTag, tvNum, tvQuestion, btnTrue, btnFalse;
        JudgeVH(View v) {
            super(v);
            tvTag = v.findViewById(R.id.tv_quiz_tag);
            tvNum = v.findViewById(R.id.tv_quiz_num);
            tvQuestion = v.findViewById(R.id.tv_quiz_question);
            btnTrue = v.findViewById(R.id.btn_true);
            btnFalse = v.findViewById(R.id.btn_false);
        }
    }
    static class AnalysisVH extends RecyclerView.ViewHolder {
        TextView tvTag, tvNum, tvQuestion, btnSubmit;
        EditText etAnswer;
        AnalysisVH(View v) {
            super(v);
            tvTag = v.findViewById(R.id.tv_quiz_tag);
            tvNum = v.findViewById(R.id.tv_quiz_num);
            tvQuestion = v.findViewById(R.id.tv_quiz_question);
            etAnswer = v.findViewById(R.id.et_answer);
            btnSubmit = v.findViewById(R.id.btn_submit);
        }
    }
    static class FillVH extends RecyclerView.ViewHolder {
        TextView tvTag, tvNum, tvQuestion, btnSubmit;
        EditText etAnswer;
        FillVH(View v) {
            super(v);
            tvTag = v.findViewById(R.id.tv_quiz_tag);
            tvNum = v.findViewById(R.id.tv_quiz_num);
            tvQuestion = v.findViewById(R.id.tv_quiz_question);
            etAnswer = v.findViewById(R.id.et_answer);
            btnSubmit = v.findViewById(R.id.btn_submit);
        }
    }
}
